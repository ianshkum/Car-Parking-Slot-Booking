package com.Park.controllers;

import com.Park.controllers.dto.ParkingDto;
import com.Park.controllers.dto.UserRegistrationDto;
import com.Park.entities.Parking;
import com.Park.entities.Booking;
import com.Park.entities.User;
import com.Park.services.ParkingService;
import com.Park.services.BookingService;
import com.Park.services.DtoMapping;
import com.Park.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller()
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private UserService userService;

    @Autowired
    DtoMapping converter;

    @Autowired
    private BookingService bookingService;

    @GetMapping()
    public String adminHomePage() {

        return "/ADM/optionsPage";
    }

    @GetMapping("/403")
    public String error403() {
        return "/errors/403";
    }



    //USER

    @GetMapping("/users")
    public String userList(Model theModel) {
        List<User> theUsers = userService.findAllUsers();
        User admin = userService.findByUsername("admin");
        User worker1=userService.findByUsername("rohit");
        User worker2=userService.findByUsername("rahul");
        User worker3=userService.findByUsername("kohli");
        
        theUsers.remove(admin);
        theUsers.remove(worker1);
        theUsers.remove(worker2);
        theUsers.remove(worker3);
        

        if(theUsers.size()==0)
            return "/ADM/empty";

        theModel.addAttribute("users", theUsers);

        return "/ADM/userList";
    }
    @GetMapping("/workers")
    public String workerList(Model theModel) {
        List<User> theWorkers = userService.findAllUsers();
        User admin = userService.findByUsername("admin");
        User user1= userService.findByUsername("user1");
        User user2= userService.findByUsername("user2");
        User user3= userService.findByUsername("user3");
        User user4 = userService.findByUsername("user4");
        theWorkers.remove(admin);
        theWorkers.remove(user1);
        theWorkers.remove(user2);
        theWorkers.remove(user3);
        theWorkers.remove(user4);
 
        if(theWorkers.size()==0)
            return "/ADM/empty";

        theModel.addAttribute("workers", theWorkers);

        return "/ADM/workerList";
    }
    
    

    @RequestMapping(path = "/delete/{id}")
    public String deleteUserById(@PathVariable("id") int id) {
        userService.deleteUserById(id);

        return "redirect:/admin/workers";
    }

    @RequestMapping(path = "/viewUser/{id}")
    public String viewUserById(Model model, @PathVariable("id") int id) {
        User user = userService.findById(id);
        List<Parking> parkings = parkingService.findByUser(user);
        List<String> apartNames = new ArrayList<String>();
        for (int i = 0; i < parkings.size(); i++) {
            apartNames.add(parkings.get(i).getParkingName());
        }
        model.addAttribute("user", user);
        model.addAttribute("apartNames", apartNames);

        return "/ADM/viewUser";
    }




    @RequestMapping(path = "/checkParking/{id}")
    public String reviewParking(Model model, @PathVariable("id") int id) throws IOException {
        Parking parking = parkingService.findById(id);
        String acceptedStatus = "accepted";
        String declinedStatus = "declined";
        model.addAttribute("parking", parking);
        model.addAttribute("accepted", acceptedStatus);
        model.addAttribute("declined", declinedStatus);

        return "/ADM/reviewParking";
    }
    
    @RequestMapping(path = "/editParking/{id}")
    public String editParking(Model model, @PathVariable("id") int id) throws IOException {
        Parking parking = parkingService.findById(id);
        String acceptedStatus = "accepted";
        String declinedStatus = "declined";
        model.addAttribute("parking", parking);
        model.addAttribute("accepted", acceptedStatus);
        model.addAttribute("declined", declinedStatus);

        return "/ADM/editParking";
    }

    @GetMapping("/pendingParkings")
    public String listPendingParkings(Model theModel) {
        List<Parking> theParkings = parkingService.findAllParkingsByStatus("pending");

        if(theParkings.size()==0)
            return "/ADM/empty";

        theModel.addAttribute("parkings", theParkings);

        return "/ADM/pendingParking";


    }
    @GetMapping(path = "/showFormForAdd")
    public String showFormForAdd(Model theModel) {

        ParkingDto parkingDto = new ParkingDto();


        if (!theModel.containsAttribute("parking")) {
            theModel.addAttribute("parking", parkingDto);
        }

        return "/ADM/parking-form";
    }
    @GetMapping("/workersAdd")
    public String showRegistrationFormforworker(Model theModel) {

        UserRegistrationDto usertDto = new UserRegistrationDto();
        if (!theModel.containsAttribute("user")) {
            theModel.addAttribute("user", usertDto);
        }
        return "/ADM/registerworker";
    }
    @GetMapping("/viewAll")
    public String listViewAll(Model theModel) {
    	List<Parking> theParkings = parkingService.findAllAccepted();
    	if (theParkings.size()==0) {
			return "/ADM/empty";
		}
    	theModel.addAttribute("parkings", theParkings);
    	return "/ADM/viewAll";
    }

    @PostMapping("/saveStatus")
    public String updateParkingStatus(@ModelAttribute("parkingId") int parkingId, @ModelAttribute("parkingStatus") String status) {
        Parking parking = parkingService.findById(parkingId);
        ParkingDto dto = converter.getParkingDtoFromParking(parking);
        dto.setStatus(status);
        parkingService.updateStatus(dto);

        return "redirect:/admin/pendingParkings";
    }

    @PostMapping("/save")
    public String addParking(@ModelAttribute("parking") @Valid ParkingDto parking, BindingResult result,
                             RedirectAttributes attr) {

        if (result.hasErrors()) {

            attr.addFlashAttribute("org.springframework.validation.BindingResult.parking", result);
            attr.addFlashAttribute("parking", parking);
            return "redirect:/admin/showFormForAdd";
        }

        parking.setStatus("accepted");

        parkingService.save(parking);

        return "redirect:/admin/pendingParkings";
    }

    //STATISTICS

    @RequestMapping(path = "/statistics")
    public String statistics(Model model) {
        List<Booking> bookings = bookingService.findAll();
        int numberOfBookings = bookings.size();
        int totalIncome = 0;
        for (Booking b : bookings) {
            long diff = b.getCheckOut().getTime() - b.getCheckIn().getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000) + 1;
            int SumDays = (int) diffDays;
            totalIncome += b.getParking().getPpn() * SumDays;
        }
        model.addAttribute("bookingsNumber", numberOfBookings);
        model.addAttribute("totalIncome", totalIncome);

        return "/ADM/statistics";
    }

    @RequestMapping("/chart")
    public String generateGraph(Model model) {
        Map<String, Integer> techMap = getTechnologyMap();
        model.addAttribute("techMap", techMap);

        return "/ADM/chart";
    }


    @RequestMapping("/pie")
    public String generatePieChart(Model model) {

        List<Booking> bookings = bookingService.findAll();

        int sumIan = 0, sumFeb = 0, sumMar = 0, sumApr = 0, sumMay = 0, sumIun = 0, sumIul = 0, sumAug = 0, sumSep = 0, sumOct = 0, sumNov = 0, sumDec = 0;
        for (Booking b : bookings) {
            switch (b.getCheckIn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue()) {
                case 1:
                    sumIan += getBookingPrice(b);
                    break;
                case 2:
                    sumFeb += getBookingPrice(b);
                    break;
                case 3:
                    sumMar += getBookingPrice(b);
                    break;
                case 4:
                    sumApr += getBookingPrice(b);
                    break;
                case 5:
                    sumMay += getBookingPrice(b);
                    break;
                case 6:
                    sumIun += getBookingPrice(b);
                    break;
                case 7:
                    sumIul += getBookingPrice(b);
                    break;
                case 8:
                    sumAug += getBookingPrice(b);
                    break;
                case 9:
                    sumSep += getBookingPrice(b);
                    break;
                case 10:
                    sumOct += getBookingPrice(b);
                    break;
                case 11:
                    sumNov += getBookingPrice(b);
                    break;
                case 12:
                    sumDec += getBookingPrice(b);
                    break;
                default:
                    break;
            }


        }


        model.addAttribute("January", sumIan);
        model.addAttribute("February", sumFeb);
        model.addAttribute("March", sumMar);
        model.addAttribute("April", sumApr);
        model.addAttribute("May", sumMay);
        model.addAttribute("June", sumIun);
        model.addAttribute("July", sumIul);
        model.addAttribute("August", sumAug);
        model.addAttribute("September", sumSep);
        model.addAttribute("October", sumOct);
        model.addAttribute("November", sumNov);
        model.addAttribute("December", sumDec);
        return "/ADM/pie";

    }

    private double getBookingPrice(Booking booking) {
        double sum = 0;
        long diff = booking.getCheckOut().getTime() - booking.getCheckIn().getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000) + 1;
        int SumDays = (int) diffDays;
        sum = booking.getParking().getPpn() * SumDays;
        return sum;
    }

    private Map<String, Integer> getTechnologyMap() {
        List<Booking> bookings = bookingService.findAll();
        List<Date> checkInDates = new ArrayList<>();
        for (Booking b : bookings) {
            checkInDates.add(b.getCheckIn());
        }

        List<Integer> months = new ArrayList<>();
        for (Date d : checkInDates) {
            months.add(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
        }

        int januaryNr = 0, februaryNr = 0, marchNr = 0, aprilNr = 0, mayNr = 0, juneNr = 0, julyNr = 0, augustNr = 0, septemberNr = 0, octoberNr = 0, novemberNr = 0, decemberNr = 0;

        for (int i = 0; i < months.size(); i++) {
            switch (months.get(i)) {
                case 1:
                    januaryNr++;
                    break;
                case 2:
                    februaryNr++;
                    break;
                case 3:
                    marchNr++;
                    break;
                case 4:
                    aprilNr++;
                    break;
                case 5:
                    mayNr++;
                    break;
                case 6:
                    juneNr++;
                    break;
                case 7:
                    julyNr++;
                    break;
                case 8:
                    augustNr++;
                    break;
                case 9:
                    septemberNr++;
                    break;
                case 10:
                    octoberNr++;
                    break;
                case 11:
                    novemberNr++;
                    break;
                case 12:
                    decemberNr++;
                    break;

                default:
                    break;
            }
        }
        Map<String, Integer> techMap = new ConcurrentHashMap<>();
        techMap.put("January", januaryNr);
        techMap.put("February", februaryNr);
        techMap.put("March", marchNr);
        techMap.put("April", aprilNr);
        techMap.put("May", mayNr);
        techMap.put("June", juneNr);
        techMap.put("July", julyNr);
        techMap.put("August", augustNr);
        techMap.put("September", septemberNr);
        techMap.put("Octomber", octoberNr);
        techMap.put("November", novemberNr);
        techMap.put("December", decemberNr);
        return techMap;
    }


}
