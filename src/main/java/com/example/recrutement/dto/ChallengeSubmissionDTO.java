package com.example.recrutement.dto;

public class ChallengeSubmissionDTO {
    private String code;
    private String langage;
    private String resultatsExecution; // JSON string
    private Double score; // percentage 0..100
    private Integer pointsTotal;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLangage() { return langage; }
    public void setLangage(String langage) { this.langage = langage; }

    public String getResultatsExecution() { return resultatsExecution; }
    public void setResultatsExecution(String resultatsExecution) { this.resultatsExecution = resultatsExecution; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Integer getPointsTotal() { return pointsTotal; }
    public void setPointsTotal(Integer pointsTotal) { this.pointsTotal = pointsTotal; }
} 