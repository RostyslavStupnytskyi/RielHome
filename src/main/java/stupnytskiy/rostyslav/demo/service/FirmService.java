package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.response.AddressResponse;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.entity.*;
import stupnytskiy.rostyslav.demo.repository.FirmRepository;


@Service
public class FirmService{

    @Autowired
    private FirmRepository firmRepository;

    @Autowired
    private RealtorService realtorService;

    public Firm findById(Long id)  {
        return firmRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Firm with id " + id + " not exists"));
    }

//    public PageResponse<AddressResponse> getFirmAddresses(PaginationRequest request){
//
//    }
}
