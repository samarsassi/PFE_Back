/*
import com.example.recrutement.entities.User;
import com.example.recrutement.services.IUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
//@RestController
@AllArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200/**")
@RequestMapping("/user")
public class UserController {

        @Autowired
        IUserService userService;

 
    //Managing roles access
        @GetMapping
        @PreAuthorize("hasRole('recrutement_user')")
        public String Hello(){
            return "hello keycloak";
        }

        @GetMapping("/hello_admin")
        @PreAuthorize("hasRole('recrutement_admin')")
        public String Hello_admin(){
            return "hello keycloak admin";
        }



    @GetMapping("/retrieve-all-user")
        public List<User> getuser() {
            return userService.retrieveAllUsers() ;
        }

        @GetMapping("/retrieve-User/{idUser}")
        public User retrieveUser(@PathVariable("idUser") Long id) {
            return userService.retrieveUser(id) ;
        }

        @PostMapping("/add-User")
        public User addUser(@RequestBody User c) {
            return userService.addUser(c) ;
        }

        @DeleteMapping("/remove-User/{idUser}")
        public void removeUser(@PathVariable("idUser") Long id) {
            userService.removeUser(id);
        }

        @PutMapping("/modify-User")
        public User modifyUser(@RequestBody User u) {
            return userService.modifyUser(u) ;

        }
    

}

 */
