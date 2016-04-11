package com.theironyard.controllers;

import com.theironyard.entities.*;
import com.theironyard.services.*;
import com.theironyard.utils.PasswordStorage;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Caroline on 4/5/16.
 */

@RestController
public class MaidMyDayController {
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    ProviderRepository providerRepository;
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    RequestRepository requestRepository;
    @Autowired
    TaskRepository taskRepository;

    Server dbui = null;

    ArrayList<Client> clients = new ArrayList<>();
    ArrayList<Provider> providers = new ArrayList<>();


    @PostConstruct
    public void init() throws SQLException {
        dbui = Server.createWebServer().start();
        Client client = new Client("Kevin", "Bacon", "1234", "kbacon@sizzling.com", "843-123-4567");
        clients.add(client);
        Provider provider = new Provider("Caroline", "Vail", "1234", "carolineevail@gmail.com", "334-669-5482");
        providers.add(provider);

    }

    @PreDestroy
    public void preDestory() {
        dbui.stop();
    }




    @RequestMapping(path = "/client", method = RequestMethod.POST)
    public Client createClient(@RequestBody Client client) throws PasswordStorage.CannotPerformOperationException {

        client.setPassword(PasswordStorage.createHash(client.getPassword()));

        return clientRepository.save(client);
    }

    @RequestMapping(path = "/client", method = RequestMethod.GET)
    public Client loginClient(HttpSession session, @RequestBody HashMap data) throws PasswordStorage.InvalidHashException, PasswordStorage.CannotPerformOperationException {

        Client client = clientRepository.findByEmail("email");

        if (client != null && PasswordStorage.verifyPassword((String) data.get("password"), client.getPassword())) {
            session.setAttribute("email", client.getEmail());
            return client;
        } else {
            return null;
        }
    }

    @RequestMapping(path = "/client", method = RequestMethod.PUT)
    public Client editClient(@RequestBody Client client, HttpSession session) throws Exception {

        if (client.getEmail().equals(session.getAttribute("email"))) {
            return clientRepository.save(client);
        } else {
            throw new Exception("You're not allowed to edit others' profiles.");
        }
    }

    @RequestMapping(path = "/client/request", method = RequestMethod.GET)
    public List<Request> clientServiceHistory(HttpSession session) {
        String clientEmail = (String) session.getAttribute("email");
        Client client = clientRepository.findByEmail(clientEmail);
        List<Request> localRequests = requestRepository.findByClient(client);
        return localRequests;
    }

    @RequestMapping(path = "/client/rating", method = RequestMethod.GET)
    public List<Rating> viewClientRatings(HttpSession session) {
        String clientEmail = (String) session.getAttribute("email");
        Client client = clientRepository.findByEmail(clientEmail);
        List<Rating> localRatings = ratingRepository.findByClient(client);
        return localRatings;
    }

    @RequestMapping(path = "/client", method = RequestMethod.DELETE)
    public void deleteClient(@RequestBody Client client) {
        clientRepository.delete(client);
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public void logout(HttpSession session) {
        session.invalidate();
    }







    @RequestMapping(path = "/provider/{id}/session", method = RequestMethod.GET)
    public Provider loginProvider(HttpSession session, @RequestBody HashMap data) throws PasswordStorage.InvalidHashException, PasswordStorage.CannotPerformOperationException {

        Provider provider = providerRepository.findByEmail("email");

        if (provider != null && PasswordStorage.verifyPassword((String) data.get("password"), provider.getPassword())) {
            session.setAttribute("email", provider.getEmail());
            return provider;
        } else {
            return null;
        }
    }

    @RequestMapping(path = "/provider", method = RequestMethod.POST)
    public Provider createProvider(@RequestBody Provider provider) throws PasswordStorage.CannotPerformOperationException {

        provider.setPassword(PasswordStorage.createHash(provider.getPassword()));

        return providerRepository.save(provider);
    }

    @RequestMapping(path = "/provider", method = RequestMethod.GET)
    public List<Provider> findMatchingProviders(@RequestBody List<Task> clientRequestedTasks) {
        List<Provider> providers = (List<Provider>) providerRepository.findAll();
        for (Provider provider : providers) {
            for (Task task : clientRequestedTasks) {
                if (!provider.getTasks().containsAll(clientRequestedTasks)) {
                    providers.remove(provider);
                }
            }
        }
        return providers;
    }

    @RequestMapping(path = "/provider/{id}", method = RequestMethod.GET)
    public Provider clientViewProviderProfile() {
        return null;
    }

    @RequestMapping(path = "/provider/profile", method = RequestMethod.GET)
    public Provider providerProfile() {
        return null;
    }

    @RequestMapping(path = "/provider", method = RequestMethod.PUT)
    public Provider editProfile(@RequestBody Provider provider, HttpSession session) throws Exception {

        if (provider.getEmail().equals(session.getAttribute("email"))) {
            return providerRepository.save(provider);
        } else {
            throw new Exception("You're not allowed to edit others' profiles.");
        }
    }

    @RequestMapping(path = "/provider/task", method = RequestMethod.PUT)
    public Provider providerSelectTasks() {
        return null;
    }

    @RequestMapping(path = "/request", method = RequestMethod.POST)
    public Request createRequest() {
        return null;
    }

    @RequestMapping(path = "/request/{id}", method = RequestMethod.DELETE)
    public Request deleteRequest() {
        return null;
    }

    @RequestMapping(path = "/provider/request", method = RequestMethod.GET)
    public List<Request> providerServiceHistory(HttpSession session) {
        String providerEmail = (String) session.getAttribute("email");
        Provider provider = providerRepository.findByEmail(providerEmail);
        List<Request> localRequests = requestRepository.findByProvider(provider);
        return localRequests;
    }

    @RequestMapping(path = "/provider/rating", method = RequestMethod.GET)
    public List<Rating> viewProviderRatings(HttpSession session) {
        String providerEmail = (String) session.getAttribute("email");
        Provider provider = providerRepository.findByEmail(providerEmail);
        List<Rating> localRatings = ratingRepository.findByProvider(provider);
        return localRatings;
    }

    @RequestMapping(path = "/provider", method = RequestMethod.DELETE)
    public void deleteProvider(@RequestBody Provider provider) {
        providerRepository.delete(provider);
    }






    @RequestMapping(path = "/notification", method = RequestMethod.POST)
    public Notification createNotification() {
        return null;
    }

    @RequestMapping(path = "/notification", method = RequestMethod.GET)
    public Notification viewNotifications() {
        return null;
    }

    @RequestMapping(path = "/notification", method = RequestMethod.DELETE)
    public Notification deleteNotification() {
        return null;
    }






    @RequestMapping(path = "/task", method = RequestMethod.GET)
    public Task populateTasks() {
        return null;
    }






    @RequestMapping(path = "/rating", method = RequestMethod.POST)
    public Rating createRating(HttpSession session) {

        return null;
    }
}
