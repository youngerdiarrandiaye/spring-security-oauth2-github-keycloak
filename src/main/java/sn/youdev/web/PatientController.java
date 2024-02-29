package sn.youdev.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import sn.youdev.entities.Patient;
import sn.youdev.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class PatientController {
    private PatientRepository patientRepository;
    @GetMapping("/user/index")
    public String index(Model model,
                        @AuthenticationPrincipal OAuth2User principal,
                        @RequestParam(name = "page",defaultValue = "0") int p,
                        @RequestParam(name = "size",defaultValue = "4")int s,
                        @RequestParam(name = "keyword",defaultValue = "")String kw
                        ){
        /***pour afficher le user connecter par email ***/
        if (principal != null) {
            String name = principal.getAttribute("name");
            model.addAttribute("userName", name);
        }
        Page<Patient> pagePatients=patientRepository.findByNomContains(kw,PageRequest.of(p,s));
        model.addAttribute("listPatients",pagePatients.getContent());
        model.addAttribute("pages",new int[pagePatients.getTotalPages()]);
        model.addAttribute("currentPage",p);
        model.addAttribute("keyword",kw);
        return "patients";
    }
    @GetMapping("/admin/delete")
    //protection de la methode
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String delete(@RequestParam(
            name = "id") Long id,
                         @RequestParam(name = "keyword",defaultValue = "") String keyword,
                         @RequestParam(name = "page",defaultValue = "0") int page){
        patientRepository.deleteById(id);
        return "redirect:/user/index?page="+page+"&keyword="+keyword;
    }
    @GetMapping("/")
    public String home(){
        return "redirect:/user/index";
    }
    @GetMapping("/formPatients")
    public String formPatient(Model model){
        model.addAttribute("patient",new Patient());
        return "formPatients";
    }
    @PostMapping("/admin/savePatient")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String savePatient(@Valid Patient patient, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "formPatients";
        }
        patientRepository.save(patient);
        return "redirect:/user/index?keyword="+patient.getNom();
    }
    @GetMapping("/admin/editPatient")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editPatient(Model model, @RequestParam(name = "id") Long id){
        Patient patient=patientRepository.findById(id).get();
        model.addAttribute("patient",patient);
        return "editPatient";
    }
}
