package sn.youdev.web;

import sn.youdev.entities.DateModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
public class DateController {
    private final List<String> daysOfWeek = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    private final List<String> dateHistory = new ArrayList<>();

    @GetMapping("/dateform")
    public String showDateForm(
            @AuthenticationPrincipal OAuth2User principal,
            @ModelAttribute DateModel dateModel, Model model) {
        /***pour afficher le user connecter par email ***/
        if (principal != null) {
            String name = principal.getAttribute("name");
            model.addAttribute("userName", name);
        }
        return "date-form";
    }

    @PostMapping("/processDate")
    public String processDate(
            @ModelAttribute DateModel dateModel, Model model) {
        LocalDate selectedDate = dateModel.getSelectedDate();
        String dateString = selectedDate.toString();

        String dayOfWeek = getDayOfWeek(dateString);
        model.addAttribute("date", dateString);
        model.addAttribute("dayOfWeek", dayOfWeek);

        // Ajouter la date à l'historique
        dateHistory.add(dateString);

        return "date-result";
    }

    @GetMapping("/dateHistory")
    public String showDateHistory(Model model) {
        model.addAttribute("dateHistory", dateHistory);
        return "date-history";
    }

    private String getDayOfWeek(String dateString) {
        // Implémenter la logique pour obtenir le jour correspondant à la date ici
        // Dans cet exemple, nous utilisons le parseur LocalDate pour obtenir le jour de la semaine
       /*** LocalDate date = LocalDate.parse(dateString);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return daysOfWeek.get(dayOfWeek.getValue() - 1);***/

        // Implémenter la logique pour obtenir le jour correspondant à la date ici
        // Utiliser un DateTimeFormatter avec le fuseau horaire de l'utilisateur
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.FRANCE);
        LocalDate date = LocalDate.parse(dateString, formatter);

        // Obtenir le jour de la semaine en utilisant le fuseau horaire de l'utilisateur
        ZoneId zoneId = ZoneId.systemDefault();
        DayOfWeek dayOfWeek = date.atStartOfDay(zoneId).getDayOfWeek();

        return daysOfWeek.get(dayOfWeek.getValue() % 7);
    }
}