package com.example.recrutement.delegate;

import com.example.recrutement.controllers.CandidatureController;
import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Entretien;
import com.example.recrutement.entities.SoumissionDefi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.EntretienRepo;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Component("scheduleInterviewDelegate")
@DelegateDescription("Schedules an interview 2 business days after challenge completion, generates a meeting link, updates the candidature, and pushes workflow variables.")
public class ScheduleInterviewDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(CandidatureController.class);

    @Autowired
    private EntretienRepo entretienRepository;

    @Autowired
    private CandidatureRepo candidatureRepository;

    @Override
    @Transactional
    public void execute(DelegateExecution execution) {
        System.out.println("[FLOWABLE] ScheduleInterviewDelegate started");

        // 1️⃣ Get workflow variables
        Integer candidatureId = (Integer) execution.getVariable("candidatureId");
        log.info("ScheduleInterviewDelegate invoked for candidatureId={} processInstanceId={}",
                candidatureId, execution.getProcessInstanceId());

        if (candidatureId == null) {
            throw new RuntimeException("Missing required workflow variable: candidatureId");
        }

        // 2️⃣ Load candidature + submission date
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found with ID: " + candidatureId));

        SoumissionDefi soumission = candidature.getSoumissionDefi();
        if (soumission == null || soumission.getSoumisLe() == null) {
            throw new RuntimeException("Challenge submission date not found for candidature " + candidatureId);
        }

        // 🔒 3️⃣ Check if interview already exists
        if (candidature.getEntretien() != null) {
            Entretien existing = candidature.getEntretien();
            log.warn("[FLOWABLE] Interview already exists for candidature {} (ID={}), skipping creation",
                    candidatureId, existing.getId());

            // Push existing values into workflow
            execution.setVariable("entretienId", existing.getId());
            execution.setVariable("interviewDate",
                    existing.getDateEntretien().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            execution.setVariable("zegoLink", existing.getLien());
            return;
        }

        // 4️⃣ Calculate interview date = submission date + 2 business days
        LocalDateTime interviewDate = soumission.getSoumisLe();
        int businessDaysAdded = 0;
        while (businessDaysAdded < 2) {
            interviewDate = interviewDate.plusDays(1);
            if (interviewDate.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    interviewDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                businessDaysAdded++;
            }
        }

        // 5️⃣ Generate meeting link
        String roomId = UUID.randomUUID().toString().substring(0, 6);
        String zegoLink = "https://jobportal.com/meeting?roomID=" + roomId;

        // 6️⃣ Create and save Entretien
        Entretien entretien = new Entretien();
        entretien.setDateEntretien(Date.from(interviewDate.atZone(ZoneId.systemDefault()).toInstant()));
        entretien.setLien(zegoLink);
        entretien.setResultat(Entretien.ResultatEntretien.EN_ATTENTE);
        entretien.setCandidature(candidature);

        Entretien savedEntretien = entretienRepository.save(entretien);

        // 7️⃣ Update candidature
        candidature.setEntretien(savedEntretien);
        candidature.setStatutEntretien(Candidature.StatutEnt.ENVOYE);
        candidatureRepository.save(candidature);

        // 8️⃣ Push variables into workflow
        String formattedDate = interviewDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        execution.setVariable("entretienId", savedEntretien.getId());
        execution.setVariable("interviewDate", formattedDate);
        execution.setVariable("zegoLink", zegoLink);

        System.out.println("[FLOWABLE] Interview scheduled successfully for: " + formattedDate);
    }
}
