package com.theironyard.controllers;

import com.theironyard.entities.*;
import com.theironyard.services.*;
import com.theironyard.utils.Constants;
import com.theironyard.utils.ObjectUpdateUtils;
import com.theironyard.utils.PasswordStorage;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;



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
    ClientRatingRepository clientRatingRepository;
    @Autowired
    RequestRepository requestRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    ProviderRatingRepository providerRatingRepository;
    @Autowired
    FileUploadRepository fileUploadRepository;

    Server dbui = null;

    @PostConstruct
    public void init() throws SQLException {
        dbui = Server.createWebServer().start();

        if (clientRepository.count() == 0) {
            Client client1 = new Client("Kevin", "Bacon", "123", "kbacon@sizzling.com", "843-123-4567", "");
            clientRepository.save(client1);
        }
        if (clientRepository.count() == 1) {
            Client client2 = new Client("Clint", "Bozic", "456", "kbacon@sizzling.com", "843-123-4567", "");
            clientRepository.save(client2);
        }
        if (providerRepository.count() == 0) {
            Provider provider1 = new Provider("Caroline", "Vail", "123", "carolineevail@gmail.com", "334-669-5482", "");
            providerRepository.save(provider1);
        }
        if (providerRepository.count() == 1) {
            Provider provider2 = new Provider("Zach", "Owens", "456", "carolineevail@gmail.com", "334-669-5482", "");
            providerRepository.save(provider2);
        }
    }

    @PreDestroy
    public void preDestory() {
        dbui.stop();
    }





    @RequestMapping(path = "/clientLogin", method = RequestMethod.POST)
    public Client login(HttpSession session, @RequestBody Client client) throws Exception {

        Client newClient = clientRepository.findByEmail(client.getEmail());

        if (client != null && PasswordStorage.verifyPassword(client.getPassword() , newClient.getPassword())) {
            session.setAttribute("email", client.getEmail());
            return newClient;
        } else {
            throw new Exception("Login failed.");
        }
    }

    @RequestMapping(path = "/client", method = RequestMethod.POST)
    public Client createClient(@RequestBody Client client) throws PasswordStorage.CannotPerformOperationException {
        client.setPassword(PasswordStorage.createHash(client.getPassword()));
        clientRepository.save(client);
        return client;
    }

    // returns a single client
    @RequestMapping(path = "/client/{id}", method = RequestMethod.GET)
    public Client getOneClient(HttpSession session, @PathVariable ("id") int id) {
        Client client = clientRepository.findOne(id);
        return client;
    }


    // returns all clients
    @RequestMapping(path = "/clients", method = RequestMethod.GET)
    public List<Client> allClients(HttpSession session) {
        return (List<Client>) clientRepository.findAll();
    }

    @RequestMapping(path = "/client", method = RequestMethod.PUT)
    public Client editClient(@RequestBody Client clientUpdates, HttpSession session) throws Exception {
        String clientEmail = (String) session.getAttribute("email");
        Client client = clientRepository.findByEmail(clientEmail);
        Client updatedClient = ObjectUpdateUtils.updateClientObject(client, clientUpdates);
        clientRepository.save(updatedClient);
        return updatedClient;
    }

    @RequestMapping(path = "/client/request", method = RequestMethod.GET)
    public List<Request> clientServiceHistory(HttpSession session) {
        String clientEmail = (String) session.getAttribute("email");
        Client client = clientRepository.findByEmail(clientEmail);
        List<Request> localRequests = requestRepository.findByClient(client);
        return localRequests;
    }

    @RequestMapping(path = "/client/rating", method = RequestMethod.GET)
    public List<ClientRating> viewClientRatings(HttpSession session) {
        String clientEmail = (String) session.getAttribute("email");
        Client client = clientRepository.findByEmail(clientEmail);
        List<ClientRating> localRatings = clientRatingRepository.findByClient(client);
        return localRatings;
    }

    @RequestMapping(path = "/client{id}", method = RequestMethod.DELETE)
    public void deleteClient(HttpSession session, @PathVariable ("id") int id) {
        Client client = clientRepository.findOne(id);
        clientRepository.delete(client);
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public void logout(HttpSession session) {
        session.invalidate();
    }







    @RequestMapping(path = "/providerLogin", method = RequestMethod.POST)
    public Provider login(HttpSession session, @RequestBody Provider provider) throws Exception {

        Provider newProvider = providerRepository.findByEmail(provider.getEmail());

        if (provider != null && PasswordStorage.verifyPassword(provider.getPassword() , newProvider.getPassword())) {
            session.setAttribute("email", provider.getEmail());
            return newProvider;
        } else {
            throw new Exception("Login failed.");
        }
    }
    // returns a single provider
    @RequestMapping(path = "/provider/{id}", method = RequestMethod.GET)
    public Provider viewingProviderProfile(HttpSession session, @PathVariable ("id") int id) {
        Provider provider = providerRepository.findOne(id);
        return provider;
    }

    // returns all providers
    @RequestMapping(path = "/providers", method = RequestMethod.GET)
    public List<Provider> allProviders(HttpSession session) {
        return (List<Provider>) providerRepository.findAll();
    }

    @RequestMapping(path = "/provider", method = RequestMethod.POST)
    public Provider createProvider(@RequestBody Provider provider) throws PasswordStorage.CannotPerformOperationException {
        provider.setPassword(PasswordStorage.createHash(provider.getPassword()));
        providerRepository.save(provider);
        return provider;
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

    @RequestMapping(path = "/provider/profile", method = RequestMethod.GET)
    public Provider providerProfile(HttpSession session) {
        String providerEmail = (String) session.getAttribute("email");
        Provider provider = providerRepository.findByEmail(providerEmail);
        return provider;
    }

    @RequestMapping(path = "/provider", method = RequestMethod.PUT)
    public Provider editProvider(@RequestBody Provider providerUpdates, HttpSession session) throws Exception {
        String providerEmail = (String) session.getAttribute("email");
        Provider provider = providerRepository.findByEmail(providerEmail);
        Provider updatedProvider = ObjectUpdateUtils.updateProviderObject(provider, providerUpdates);
        providerRepository.save(updatedProvider);
        return updatedProvider;
    }

    @RequestMapping(path = "/provider/task", method = RequestMethod.PUT)
    public void providerSelectTasks(@RequestBody Provider provider) {
        providerRepository.save(provider);
    }

    @RequestMapping(path = "/provider/request", method = RequestMethod.GET)
    public List<Request> providerServiceHistory(HttpSession session) {
        String providerEmail = (String) session.getAttribute("email");
        Provider provider = providerRepository.findByEmail(providerEmail);
        List<Request> localRequests = requestRepository.findByProvider(provider);
        return localRequests;
    }

    @RequestMapping(path = "/provider/rating", method = RequestMethod.GET)
    public List<ProviderRating> viewProviderRatings(HttpSession session) {
        String providerEmail = (String) session.getAttribute("email");
        Provider provider = providerRepository.findByEmail(providerEmail);
        List<ProviderRating> localRatings = providerRatingRepository.findByProvider(provider);
        return localRatings;
    }

    @RequestMapping(path = "/provider/{id}", method = RequestMethod.DELETE)
    public void deleteProvider(HttpSession session, @PathVariable ("id") int id) {
        Provider provider = providerRepository.findOne(id);
        providerRepository.delete(provider);
    }

    @RequestMapping(path = "/provider/{id}/isOnline", method = RequestMethod.PUT)
    public Provider toggleIsOnline(@PathVariable ("id") int id) {
        Provider provider = providerRepository.findOne(id);
        provider.setIsOnline(!provider.getIsOnline());
        providerRepository.save(provider);
        return provider;
    }






    @RequestMapping(path = "/request/provider/{id}", method = RequestMethod.POST)
    public void createRequest(@RequestBody Request request, HttpSession session, @PathVariable ("id") int id) {
        String clientEmail = (String) session.getAttribute("email");
        Client client = clientRepository.findByEmail(clientEmail);
        Provider provider = providerRepository.findOne(id);
        request.setRequestDateTime(LocalDateTime.now());
        request.setClient(client);
        request.setProvider(provider);
        requestRepository.save(request);
    }

    @RequestMapping(path = "/request/{id}/isDone", method = RequestMethod.PUT)
    public void requestCompleted(@PathVariable ("id") int id) {
        Request request = requestRepository.findOne(id);
        request.setIsDone(true);
        requestRepository.save(request);
        Notification notification = new Notification(Constants.NOTIFICAITON_TEXT, LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @RequestMapping(path = "/request/{id}", method = RequestMethod.DELETE)
    public void deleteRequest(@PathVariable ("id") int id) {
        requestRepository.delete(id);
    }






    @RequestMapping(path = "/notification", method = RequestMethod.GET)
    public List<Notification> viewNotifications(HttpSession session) {
        String clientEmail = (String) session.getAttribute("email");
        Client client = clientRepository.findByEmail(clientEmail);
        List<Notification> clientNotifications = notificationRepository.findByClient(client);
        return clientNotifications;
    }

    @RequestMapping(path = "/notification", method = RequestMethod.DELETE)
    public Notification deleteNotification() {
        return null;
    }





    @RequestMapping(path = "/rating/provider/{id}", method = RequestMethod.POST)
    public void createProviderRating(HttpSession session, @PathVariable ("id") int id, @RequestBody ProviderRating rating) {
        String clientEmail = (String) session.getAttribute("email");
        Client client = clientRepository.findByEmail(clientEmail);
        Provider provider = providerRepository.findOne(id);
        providerRatingRepository.save(rating);
    }

    @RequestMapping(path = "/rating/client/{id}", method = RequestMethod.POST)
    public void createClientRating(HttpSession session, @PathVariable ("id") int id, @RequestBody ClientRating rating) {
        String providerEmail = (String) session.getAttribute("email");
        Provider provider = providerRepository.findByEmail(providerEmail);
        Client client = clientRepository.findOne(id);
        clientRatingRepository.save(rating);
    }




    @RequestMapping(path = "/task", method = RequestMethod.GET)
    public Task populateTasks() {
        return null;
    }




    //////
    ////// Messing with file upload
    //////
    ////// Credit : http://www.journaldev.com/2573/spring-mvc-file-upload-example-tutorial-single-and-multiple-files
    //////
    ////// Need to figure out how to ONLY allow jpeg, png, etc... only photos. No mp4, mp3, mov, etc...
    ////// We did this in class
    //////
    ////// Also need to set a max file size limit
    ////// We've also done this in class, but it might be accomplished on the frontend
    //////



    /**
     * Upload single file using Spring Controller
     */
    @RequestMapping(path = "/fileUpload", method = RequestMethod.POST)
    public void upload(MultipartFile photo, HttpSession session) throws Exception {

        Client client = clientRepository.findByEmail((String) session.getAttribute("email"));
        Provider provider = providerRepository.findByEmail((String) session.getAttribute("email"));

        if (!photo.getContentType().startsWith("image")) {
            throw new Exception("You can only upload images");
        }

        // not sure if this is the correct directory
        File photoFile = File.createTempFile("image", photo.getOriginalFilename(), new File("public"));
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(photo.getBytes());

        FileUpload newPhoto = new FileUpload(photo.getOriginalFilename());

        if (client != null) {
            newPhoto.setFileName(client.getEmail() + " " + "profile image");
            newPhoto.setClient(client);
        } else if (provider != null) {
            newPhoto.setFileName(provider.getEmail() + " " + "profile image");
            newPhoto.setProvider(provider);
        }

        fileUploadRepository.save(newPhoto);
    }
}
