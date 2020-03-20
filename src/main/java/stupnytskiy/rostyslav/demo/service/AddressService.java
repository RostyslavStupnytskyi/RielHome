package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.AddressRequest;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AddressResponse;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.dto.response.RealtorResponse;
import stupnytskiy.rostyslav.demo.entity.Address;
import stupnytskiy.rostyslav.demo.entity.Firm;
import stupnytskiy.rostyslav.demo.entity.Realtor;
import stupnytskiy.rostyslav.demo.repository.AddressRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private RegionService regionService;

    @Autowired
    private StreetTypeService streetTypeService;

    @Autowired
    private UserService userService;

    public Address findById(Long id){
        return addressRepository.findById(id).orElseThrow(() -> new IllegalArgumentException());
    }

    public Address findByIdAndFirm(Long id){
        final Firm firm = userService.findByLogin((String)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getFirm();
        return addressRepository.findByIdAndFirm(id, firm);
    }

    public PageResponse<AddressResponse> finsAllByFirmId(Long id, PaginationRequest request){
        final Page<Address> page = addressRepository.findAllByFirmId(id, request.mapToPageable());
        return new PageResponse<>(page.getContent().stream().map(AddressResponse::new).collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages()
        );
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

    public void save(AddressRequest request) {
        final Firm firm = userService.findByLogin((String)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getFirm();
        Address address = addressRequestToAddress(request, null);
        address.setFirm(firm);
        addressRepository.save(address);
    }

    public void update(Long id, AddressRequest request) {
        addressRepository.save(addressRequestToAddress(request, findById(id)));
    }


    public void delete(Long id) {
        addressRepository.delete(findByIdAndFirm(id));
    }

    public List<AddressResponse> findAll(Long id) {
        return addressRepository.findAllByFirmId(id).stream().map(AddressResponse::new).collect(Collectors.toList());
    }
}

