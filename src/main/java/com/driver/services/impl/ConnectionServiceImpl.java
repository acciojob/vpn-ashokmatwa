package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        User user = userRepository2.findById(userId).get();

        if(user.getConnected()) throw new Exception("Already connected");

        CountryName originalCountryName = user.getOriginalCountry().getCountryName();
        String userCountryName = originalCountryName.toCode();
        if(userCountryName.equals(countryName))
            return user;

        CountryName countryName1 = null;
        if(countryName.equals("ind")) countryName1 = CountryName.IND;
        if(countryName.equals("aus")) countryName1 = CountryName.AUS;
        if(countryName.equals("usa")) countryName1 = CountryName.USA;
        if(countryName.equals("chi")) countryName1 = CountryName.CHI;
        if(countryName.equals("jpn")) countryName1 = CountryName.JPN;

        // yeh to original country ka hi hai
//        ServiceProvider serviceProvider = user.getCountry().getServiceProvider();

        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();

        //As user does not have a serviceProvider
        if (serviceProviderList.size() == 0) throw new Exception("Unable to connect");

        ServiceProvider serviceProviderTobeSet = null;
        Country countryToBeSet = null;
        for (ServiceProvider currentServiceProvider : serviceProviderList){
            List<Country> countryList = currentServiceProvider.getCountryList();
            for (Country currentCountry : countryList){
                if(currentCountry.getCountryName().equals(countryName1)){
                    serviceProviderTobeSet = currentCountry.getServiceProvider(); // = currentServiceProvider
                    countryToBeSet = currentCountry;
                    //serviceProviderList.remove(currentServiceProvider);
                    break;
                }
            }
            if(serviceProviderTobeSet != null) break;
        }
        //serviceProvider does not have given country
        if (serviceProviderTobeSet == null) throw new Exception("Unable to connect");

        //establish  the connection
        Connection connection = new Connection();

        connection.setUser(user); // foreign
        connection.setServiceProvider(serviceProviderTobeSet); // foreign

        serviceProviderTobeSet.getConnectionList().add(connection); // foreign
        //serviceProviderList.add(serviceProviderTobeSet);

        user.setConnected(true);
        user.setOriginalCountry(countryToBeSet);
        String maskedIp = countryToBeSet.getCode() + "." + serviceProviderTobeSet.getId() + "." + user.getId();
        user.setMaskedIp(maskedIp);
        user.getConnectionList().add(connection); // bidirectional
        //user.setServiceProviderList(serviceProviderList);

        userRepository2.save(user); // cascade --> connection

        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {

        User user = userRepository2.findById(userId).get();
        if (!user.getConnected()) throw new Exception("Already disconnected");

        List<Connection> connectionList = user.getConnectionList();
        for (Connection connection : connectionList){
            if(connection.getUser().getId() == userId){
                ServiceProvider serviceProvider = connection.getServiceProvider();
                serviceProvider.getConnectionList().remove(connection);
                connection.setUser(null);
                connection.setServiceProvider(null);
                connectionList.remove(connection);
                break;
            }
        }

        user.setConnectionList(connectionList);
        user.setConnected(false);
        user.setMaskedIp(null);

        userRepository2.save(user);
        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {

        User senderUser = userRepository2.findById(senderId).get();
        User receiverUser = userRepository2.findById(receiverId).get();

        return senderUser;
    }
}
