package com.driver.services.impl;

import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    //Handle exceptions in case of invalid input or missing information.
    @Override
    public Admin register(String username, String password) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(password);
        //no service provider needed
        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {
        Admin admin = adminRepository1.findById(adminId).get();

        //either service provider create using new keyword and then set its name
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setName(providerName);
        serviceProvider.setAdmin(admin);//foreign

        //OR find service provider By name
        /*ServiceProvider serviceProvider = serviceProviderRepository1.findByName(providerName);
        serviceProvider.setAdmin(admin);//foreign*/

        admin.getServiceProviders().add(serviceProvider);//bi-directional
        adminRepository1.save(admin); // cascade --> serviceP
        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{

        String modifiedName = countryName.toLowerCase();
        if (modifiedName.length() != 3) throw new Exception("Country not found");
        //ind, aus, usa, chi, jpn.
        if(     modifiedName.equals("ind") ||
                modifiedName.equals("aus") ||
                modifiedName.equals("usa") ||
                modifiedName.equals("chi") ||
                modifiedName.equals("jpn")) {

            Country country = new Country(); // new country object is created
            ServiceProvider serviceProvider = serviceProviderRepository1.findById(serviceProviderId).get();

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

            country.setServiceProvider(serviceProvider); // foreign
            serviceProvider.getCountryList().add(country); // bi-directional

            serviceProviderRepository1.save(serviceProvider); // cascade --> country

            return  serviceProvider;
        }

        else{
            throw new Exception("Country not found");
        }

    }
}
