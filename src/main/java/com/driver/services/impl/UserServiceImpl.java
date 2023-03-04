package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{

        String modifiedName = countryName.toLowerCase();
        if(     modifiedName.equals("ind") ||
                modifiedName.equals("aus") ||
                modifiedName.equals("usa") ||
                modifiedName.equals("chi") ||
                modifiedName.equals("jpn")){

            User user = new User();
            Country country = new Country();

            if(modifiedName.equals("ind")){
                country.setCountryName(CountryName.IND);
                country.setCode(CountryName.IND.toCode());
            }
            if(modifiedName.equals("aus")){
                country.setCountryName(CountryName.AUS);
                country.setCode(CountryName.AUS.toCode());
            }
            if(modifiedName.equals("usa")){
                country.setCountryName(CountryName.USA);
                country.setCode(CountryName.USA.toCode());
            }
            if(modifiedName.equals("chi")){
                country.setCountryName(CountryName.CHI);
                country.setCode(CountryName.CHI.toCode());
            }
            if(modifiedName.equals("jpn")){
                country.setCountryName(CountryName.JPN);
                country.setCode(CountryName.JPN.toCode());
            }

            country.setUser(user);//foreign

            user.setOriginalCountry(country); // bidirectional
            user.setUsername(username);
            user.setPassword(password);
            String originalIp = country.getCode()+"."+user.getId();//"countryCode.userId"
            user.setOriginalIp(originalIp);
            user.setMaskedIp(null);
            user.setConnected(false);

            //set --> user --> connection, serviceP ??
            // serviceP --> subscribe function
            userRepository3.save(user); // cascade --> country
            return user;
        }
        else {
            throw new Exception();
        }
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user = userRepository3.findById(userId).get();
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();

        serviceProvider.getUsers().add(user); //foreign
        user.getServiceProviderList().add(serviceProvider); // bidirectional

        //user ->connection ??
//        user.setOriginalIp(originalIp);
//        user.setMaskedIp(null);
//        user.setConnected(false);

        userRepository3.save(user); // cascade --> serviceP
        return user;
    }
}
