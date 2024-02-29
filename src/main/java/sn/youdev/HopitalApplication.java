package sn.youdev;

import lombok.NoArgsConstructor;
import sn.youdev.entities.Patient;
import sn.youdev.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import java.util.Date;

@SpringBootApplication
@NoArgsConstructor
public class HopitalApplication implements CommandLineRunner {

    @Autowired
    private PatientRepository patientRepository;
    public HopitalApplication(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(HopitalApplication.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
       patientRepository.save(new Patient(null,"Mohamed",new Date(),false,4000));
       patientRepository.save(new Patient(null,"Hanane",new Date(),false,432));
       patientRepository.save(new Patient(null,"Imane",new Date(),true,340));

    }

    //@Bean
    CommandLineRunner commandLineRunner(JdbcUserDetailsManager jdbcUserDetailsManager){
        PasswordEncoder passwordEncoder = passwordEncoder();
        return args -> {
            jdbcUserDetailsManager.createUser(User.withUsername("user1").password(passwordEncoder.encode("1234")).roles("USER").build());
            jdbcUserDetailsManager.createUser(User.withUsername("user2").password(passwordEncoder.encode("1234")).roles("USER").build());
            jdbcUserDetailsManager.createUser(User.withUsername("admin22").password(passwordEncoder.encode("1234")).roles("USER","ADMIN").build());
        };
    }
    /***
     * pour les mots de passe
     * **/
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
