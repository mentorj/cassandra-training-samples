package com.javaxpert.demos.trainings.cassandra.ui;

import com.google.common.base.Charsets;
import com.google.common.hash.*;
import com.javaxpert.demos.trainings.cassandra.bloomfilters.bo.Person;

import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LargeAgendaDemo {
    private final static int MAX_SIZE=750;
    private enum PROVIDER{
        GOOGLE,
        HOTMAIL,
        ORANGE,
        LAPOSTE,
        NETCOURRIER
    }

    private enum LASTNAME{
        DOE,
        MARTIN,
        OCONNOR,
        COLAS,
        SUSA,
        GONCALVES,
        LEE,
        GUPTA
    }

    private enum FIRSTNAME{
        TITI,
        TUTU,
        TATA,
        JOHN,
        JOHANN,
        PATRICK,
        MARTHA,
        SUSAN,
        MARY,
        ALICE,
        JULIAN
    }
    private static List<String> firstNames = null;
    private static List<String> lastNames = null;
    private static List<String> domainNames = null;

    static {
        firstNames = Arrays.stream(FIRSTNAME.values()).map(first -> first.name())
                .collect(Collectors.toList());
        lastNames = Arrays.stream(LASTNAME.values()).map(last -> last.name())
                .collect(Collectors.toList());
        domainNames = Arrays.stream(PROVIDER.values()).map(provider -> provider.name())
                .collect(Collectors.toList());
    }
    private static Person createPerson(){
        int age =15+ThreadLocalRandom.current().nextInt(100);
        Instant birth= Instant.now().minus(Duration.ofDays(365*age));
        String firstname = firstNames.get(ThreadLocalRandom.current().nextInt(FIRSTNAME.values().length));
        String lastname = lastNames.get(ThreadLocalRandom.current().nextInt(LASTNAME.values().length));
        String domain = domainNames.get(ThreadLocalRandom.current().nextInt(PROVIDER.values().length));
        Person p = new Person(firstname+"."+lastname+"@"+domain+".com",firstname,lastname,birth);
        System.out.println("Person generated : " + p.toString());
        return p;
    }
    public static void main(String[] args) {
        Set<Person> agenda = new HashSet<>(100000);
        int[] indexes = new int[MAX_SIZE];
        agenda = Arrays.stream(indexes).mapToObj(i -> createPerson()).collect(Collectors.toSet());
        System.out.println("created agenda");
        Funnel<Person> personFunnel = new Funnel<Person>() {
            @Override
            public void funnel(Person person, PrimitiveSink primitiveSink) {
                primitiveSink
                        .putString(person.getFirstName(), Charsets.UTF_8)
                        .putString(person.getLastName(), Charsets.UTF_8)
                        .putString(person.getPersonalMail(), Charsets.UTF_8);
//                        .putLong(person.getDateOfBirth().getEpochSecond());
            }

        };
        // hash using the google Murmur3 implementation for reduced collisions
        final HashFunction hf= Hashing.murmur3_128();
        // now maps the agenda into the sets of corresponding hashcodes
        Stream<Object> hashcodes =  agenda.stream().map(p ->hf.hashObject(p,personFunnel).hashCode());
        hashcodes.forEach(System.out::println);


        BloomFilter<Person> filter = BloomFilter.create(personFunnel,MAX_SIZE*5);
        agenda.stream().forEach(p-> filter.put(p));
        Person toto = new Person("toto@tutu.com", "toto", "toto",
                LocalDate.of(2002, Month.NOVEMBER,18)
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
        );
        boolean mightContainToto = filter.mightContain(toto);
        if(!mightContainToto){
            System.out.println(toto.toString() + " is not in the BloomFilter");
        }
        else{
            System.out.println(toto.toString() + " may be in the filter..beware of false positive");
        }
        System.out.println("rechercher un utilisateur dans le BloomFilter, regarder votre console avant,saisissez prenom/nom/email...");
        Scanner scanner= new Scanner(System.in);
        String line_read = "";
        boolean end_loop=false;
        while(!end_loop) {
            line_read = scanner.next();
            StringTokenizer st = new StringTokenizer(line_read, "/");
            Person target = new Person(st.nextToken(), st.nextToken(), st.nextToken(), Instant.now());
            boolean person_fetched = filter.mightContain(target);
            System.out.println("Person fetched from filter ?" + (person_fetched?"may be in the filter":"definitely not"));
        }
        System.out.println("Leaving..Bye");
        System.exit(1);
    }

}
