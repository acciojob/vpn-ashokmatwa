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
        int count = Integer.MAX_VALUE;
        for (ServiceProvider currentServiceProvider : serviceProviderList){
            List<Country> countryList = currentServiceProvider.getCountryList();
            for (Country currentCountry : countryList){
                if(currentCountry.getCountryName().equals(countryName1) && count > currentServiceProvider.getId()){
                    serviceProviderTobeSet = currentServiceProvider;
                    countryToBeSet = currentCountry;
                    count = currentServiceProvider.getId();
                    //serviceProviderList.remove(currentServiceProvider);
                }
            }
        }
        //serviceProvider does not have given country
//        if (serviceProviderTobeSet == null) throw new Exception("Unable to connect");

        //establish  the connection
        if(serviceProviderTobeSet != null) {
            Connection connection = new Connection();

            connection.setUser(user); // foreign
            connection.setServiceProvider(serviceProviderTobeSet); // foreign

            serviceProviderTobeSet.getConnectionList().add(connection); // foreign
            //serviceProviderList.add(serviceProviderTobeSet);

            user.setConnected(true);
//        user.setOriginalCountry(countryToBeSet);
            String maskedIp = countryToBeSet.getCode() + "." + serviceProviderTobeSet.getId() + "." + user.getId();
            user.setMaskedIp(maskedIp);
            user.getConnectionList().add(connection); // bidirectional
            //user.setServiceProviderList(serviceProviderList);

            userRepository2.save(user); // cascade --> connection
            serviceProviderRepository2.save(serviceProviderTobeSet);
        }

        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {

        User user = userRepository2.findById(userId).get();
        if (!user.getConnected()) throw new Exception("Already disconnected");

        /*List<Connection> connectionList = user.getConnectionList();
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
        user.setConnectionList(connectionList);*/

        user.setConnected(false);
        user.setMaskedIp(null);

        userRepository2.save(user);
        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {

        User senderUser = userRepository2.findById(senderId).get();
        User receiverUser = userRepository2.findById(receiverId).get();

//        if(senderUser.getOriginalCountry().equals(receiverUser.getOriginalCountry())){
//            if(receiverUser.getConnected())
//        }
        if(receiverUser.getConnected()){
            //If the receiver is connected to a vpn, his current country is the one he is connected to.
            String str = receiverUser.getMaskedIp();//currentCountry |not using direct bcoz that is ORIGINAL country
            String code = str.substring(0,3); //chopping country code = cc

            //If the sender's original country matches receiver's current country,
            // we do not need to do anything as they can communicate. Return the sender as it is.
            if(code.equals(senderUser.getOriginalCountry().getCode()))
                return senderUser;
            else {
                String countryName = "";

                if (code.equals(CountryName.IND.toCode())) countryName = CountryName.IND.toString();
                if (code.equals(CountryName.AUS.toCode())) countryName = CountryName.AUS.toString();
                if (code.equals(CountryName.USA.toCode())) countryName = CountryName.USA.toString();
                if (code.equals(CountryName.CHI.toCode())) countryName = CountryName.CHI.toString();
                if (code.equals(CountryName.JPN.toCode())) countryName = CountryName.JPN.toString();


                User user = connect(senderId, countryName); //function calling

                if (!user.getConnected())
                    throw new Exception("Cannot establish communication");
                else return user;
            }
        }
        else{
            //If the receiver is not connected to vpn, his current country is his original country.
            if(receiverUser.getOriginalCountry().equals(senderUser.getOriginalCountry()))
                return senderUser;

            String countryName = receiverUser.getOriginalCountry().getCountryName().toString();
            User user1 =  connect(senderId, countryName); //function calling

            if (!user1.getConnected())
                throw new Exception("Cannot establish communication");
            else return user1;
        }
    }
}
