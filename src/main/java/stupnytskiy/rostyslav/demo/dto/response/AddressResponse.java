package stupnytskiy.rostyslav.demo.dto.response;

import lombok.Getter;
import lombok.Setter;
import stupnytskiy.rostyslav.demo.entity.Address;
import stupnytskiy.rostyslav.demo.entity.Region;
import stupnytskiy.rostyslav.demo.entity.StreetType;

import javax.persistence.Column;
import javax.persistence.ManyToOne;

@Getter
@Setter
public class AddressResponse {

    private Long id;
    private String regionName;
    private String settlement;
    private String streetType;
    private String streetName;
    private String streetNumber;

    public AddressResponse(Address address){
        id = address.getId();
        regionName  = address.getRegion().getName();
        settlement = address.getSettlement();
        streetType = address.getStreetType().getName();
        streetName = address.getStreetName();
        streetNumber = address.getStreetNumber();
    }
}
