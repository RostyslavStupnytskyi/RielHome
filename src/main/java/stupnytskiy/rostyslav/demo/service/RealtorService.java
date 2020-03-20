package stupnytskiy.rostyslav.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import stupnytskiy.rostyslav.demo.dto.request.PaginationRequest;
import stupnytskiy.rostyslav.demo.dto.response.PageResponse;
import stupnytskiy.rostyslav.demo.dto.response.RealtorResponse;
import stupnytskiy.rostyslav.demo.entity.Realtor;
import stupnytskiy.rostyslav.demo.repository.RealtorRepository;

import java.util.stream.Collectors;


@Service
public class RealtorService{

    @Autowired
    private RealtorRepository realtorRepository;

    @Autowired
    private FirmService firmService;

    @Autowired
    private RegionService regionService;


    public Realtor findById(Long id)  {
        return realtorRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Realtor with id " + id + " not exists"));
    }

    public PageResponse<RealtorResponse> findByFirm(PaginationRequest request){
        String login = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final Page<Realtor> page = realtorRepository.findAllByFirmUserLogin(login, request.mapToPageable());
        return new PageResponse<>(page.getContent().stream().map(RealtorResponse::new).collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
