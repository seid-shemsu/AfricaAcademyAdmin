package com.seid.academyadmin;

public class CourseObject {
    String course_name, progress, img_url;
    String code;
    double rating;
    int students;

    public CourseObject(String course_name, String img_url, double rating, String code, String progress) {
        this.course_name = course_name;
        this.progress = progress;
        this.img_url = img_url;
        this.code = code;
        this.rating = rating;
        this.students = students;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getStudents() {
        return students;
    }

    public void setStudents(int students) {
        this.students = students;
    }
}
