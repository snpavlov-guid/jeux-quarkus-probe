package Entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SmokeTest {
    @Test
    void createsLeagueAndSeasonLabel() {
        League league = new League();
        league.setName("Premier");

        Tournament tournament = new Tournament();
        tournament.setStYear(2024);
        tournament.setFnYear(null);

        assertThat(league.getName()).isEqualTo("Premier");
        assertThat(tournament.getSeasonLabel()).isEqualTo("2024");
    }
}
