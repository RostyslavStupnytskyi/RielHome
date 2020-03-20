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
    private Long regionId;
    private String settlement;
    private String streetType;
    private Long streetTypeId;
    private String streetName;
    private String streetNumber;

    public AddressResponse(Address address){
        id = address.getId();
        regionName  = address.getRegion().getName();
        regionId = address.getRegion().getId();
        settlement = address.getSettlement();
        streetType = address.getStreetType().getName();
        streetTypeId = address.getStreetType().getId();
        streetName = address.getStreetName();
        streetNumber = address.getStreetNumber();
    }
}
