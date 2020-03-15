package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.AddressRequest;
import stupnytskiy.rostyslav.demo.entity.Address;
import stupnytskiy.rostyslav.demo.repository.AddressRepository;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private RegionService regionService;

    @Autowired
    private StreetTypeService streetTypeService;

    public Address findById(Long id){
        return addressRepository.findById(id).orElseThrow(() -> new IllegalArgumentException());
    }

    public Address addressRequestToAddress(AddressRequest request, Address address){
        if(address == null) address = new Address();
        address.setRegion(regionService.findById(request.getRegionId()));
        address.setStreetType(streetTypeService.findById(request.getStreetTypeId()));
        address.setSettlement(request.getSettlement());
        address.setStreetNumber(request.getStreetNumber());
        address.setStreetName(request.getStreetName());
        return address;
    }
}

