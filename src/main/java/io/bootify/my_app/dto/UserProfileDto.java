package io.bootify.my_app.dto;

import io.bootify.my_app.domain.Profile;
import java.time.LocalDate;

public class UserProfileDto {
    private Profile profile;
    private LocalDate startDate;
    private LocalDate endDate;

    public UserProfileDto() {
    }

    public UserProfileDto(Profile profile, LocalDate startDate, LocalDate endDate) {
        this.profile = profile;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
