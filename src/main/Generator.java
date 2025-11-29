package main;

import com.github.javafaker.Faker;
import structure.HeapFile;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

public class Generator {
    private static final int PEOPLE_COUNT = 1000;

    private HeapFile<Patient> heapFile;
    private Faker faker;
    private Set<String> peopleIDs;

    public Generator(HeapFile<Patient> heapFile) {
        this.faker = new Faker();
        this.peopleIDs = new HashSet<>();
        this.heapFile = heapFile;
    }

    public void generatePeople() {
        int i = 0;
        while (i < PEOPLE_COUNT) {
            Patient patient = this.generatePatient();
//            System.out.println(patient.getOutput());
            this.heapFile.insert(patient);
//            System.out.println("----------------------");
            ++i;
        }
        System.out.println(PEOPLE_COUNT + " Patients generated.");
    }

    public Patient generatePatient() {
        String name, surname, personID;
        LocalDate date;
        do {
            name = faker.name().firstName();
        } while (name.length() > 15);
        do {
            surname = faker.name().lastName();
        } while (surname.length() > 14);
        date = faker.date().between(Date.valueOf("1945-01-01"), Date.valueOf("2025-11-21"))
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
//            date = this.faker.date().birthday(5, 100).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        do {
            personID = this.faker.lorem().characters(1, 10, true, true);
        } while (!this.peopleIDs.add(personID));

        return new Patient(name, surname, date, personID);
    }
}
