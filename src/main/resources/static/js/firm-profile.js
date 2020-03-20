const API_URL = 'http://localhost:8080';
const $avatarHolder = $('#firm-avatar-container');
const $firmNameInput = $('#firm-name-input');
const $firmPhoneInput = $('#firm-phone-input');
const $firmEmailInput = $('#firm-email-input');
const $realtorsCount = $('#realtors-count-container');
const $realtiesCount = $('#realties-count-container');

const $editProfileBtn = $('#edit-profile-btn');
const $addressesBtn = $('#addresses-btn');

const $realtorsHolder = $('#realtors-container-content');
const $addressesHolder = $('#addresses-container-content');
const $wishesHolder = $('#wishes-container-content');
const $answersHolder = $('#answers-container-content');
const $publicationsHolder = $('#publications-container-content');

// const $openAddressModal = $('.open-address-modal');
const $editAddressBtn = $('.edit-address-btn');
const $deleteAddressBtn = $('.delete-address-btn');

const $addressModal = $('#address-modal');
const $addressDeleteModal = $('#address-delete-modal');

const $addressRegionSelect = $('#address-region-select');
const $addressStreetTypeSelect = $('#address-street-type-select');
const $addressModalStreetNameInput = $('#address-street-name-input');
const $addressModalStreetNumberInput = $('#address-street-number-input');
const $addressModalSettlementInput = $('#address-settlement-input');
const $saveAddressBtn = $('#save-address-btn');

$(document).ready(() => {
    if (!window.localStorage.getItem('user_name')) {
        window.location.href = `${API_URL}/sign-in`;
    } else {
        $.ajax({
            url: `${API_URL}/user/firm`,
            contentType: 'application/json',
            headers: {
                "Authorization": `Bearer ${window.localStorage.getItem('user_token')}`
            },
            type: 'get',
            success: function (response) {
                console.log(response);
                appendProfileInfoIntoContainer(response);
                realtorsRequest();
                addressesRequest();
                regionRequest();
                streetTypeRequest();
            }
        })
    }
});

function realtorsRequest() {
    $.ajax({
        url: `${API_URL}/realtor/firm?direction=DESC&field=id&page=0&size=5`,
        contentType: 'application/json',
        headers: {
            "Authorization": `Bearer ${window.localStorage.getItem('user_token')}`
        },
        type: 'get',
        success: function (response) {
            console.log(response);
            if (response.data.length === 0) appendNoRealtorsLabel();
            else appendRealtorsToContainer(response);
        }
    })
}
$('.close-modal').click(() => {
   clearAddressModal();
});

function appendNoRealtorsLabel() {
    $realtorsHolder.append(`
    <div class="no-items-label"><label>Your firm haven't realtors</label></div>
    `);
}

function appendRealtorsToContainer(realtors) {

}

$addressesBtn.click(()=>{

});


function addressesRequest() {
    let id = $('#firm-id-input').val();
    $.ajax({
        url: `${API_URL}/address?direction=DESC&field=id&id=${id}&page=0&size=5`,
        contentType: 'application/json',
        type: 'get',
        success: function (response) {
            console.log(response);
            if (response.data.length === 0) appendNoAddressesLabel();
            else {
                appendAddressesToContainer(response.data, $addressesHolder);
                actionsOnEditAddressBtn();
                actionsOnDeleteAddressBtn();
            }
        }
    })
}

function appendNoAddressesLabel() {
    $addressesHolder.append(`
    <div class="no-items-label"><label>Your firm haven't addresses</label></div>
    `);
}

function appendAddressesToContainer(addresses) {
    for (let address of addresses) {
        $addressesHolder.append(`
            <div class="address-item">
            <div class="address-item-info">
                <div class="address-item-place">
                   ${address.settlement}, ${address.regionName} region
                </div>
                <div class="address-item-street">
                 ${address.streetType} ${address.streetName}, ${address.streetNumber}
                </div>
            </div>
            <div class="address-item-actions">
           <button class="edit-address-btn column-content-btn" value="${address.id}">Edit</button>
           <button class="delete-address-btn column-content-btn" value="${address.id}">Delete</button>
            </div>
            </div>
        `);
    }
}

$('#add-address-btn').click(() => {
    $addressModal.css('display', 'block');
});

function actionsOnEditAddressBtn() {
    $('.edit-address-btn').click((e) => {
        $addressModal.css('display', 'block');
        let $btn = $(e.target);
        let id = $btn.val();
        $.ajax({
            url: `${API_URL}/address/one?id=` + id,
            contentType: 'application/json',
            type: 'get',
            success: function (response) {
                $('#address-modal-id-input').val(response.id);
                $addressStreetTypeSelect.val(response.streetTypeId);
                $addressRegionSelect.val(response.regionId);
                $addressModalSettlementInput.val(response.settlement);
                $addressModalStreetNameInput.val(response.streetName);
                $addressModalStreetNumberInput.val(response.streetNumber);
            }
        })
    })
}

function actionsOnDeleteAddressBtn() {
    $('.delete-address-btn').click((e) => {
        let $btn = $(e.target);
        let id = $btn.val();
        $addressDeleteModal.css('display', 'block');
        actionsOnConfirmDeleteAddressBtn(id);
    });
}

function actionsOnConfirmDeleteAddressBtn(id) {
    $('#address-delete-modal-btn').click(()=>{
        $.ajax({
            url: `${API_URL}/address?id=` + id,
            contentType: 'application/json',
            headers: {
                "Authorization": `Bearer ${window.localStorage.getItem('user_token')}`
            },
            type: 'delete',
            success: function () {
                $addressDeleteModal.css('display', 'none');
                $addressesHolder.html('');
                addressesRequest($addressesHolder);
            }
        })
    })
}

$editProfileBtn.click(() => {
    if ($firmNameInput.prop('disabled')) {
        $firmNameInput.attr('disabled', false);
        $firmPhoneInput.attr('disabled', false);
        $firmEmailInput.attr('disabled', false);
        $editProfileBtn.html('Save changes');
    } else {
        $firmNameInput.attr('disabled', true);
        $firmPhoneInput.attr('disabled', true);
        $firmEmailInput.attr('disabled', true);
        $editProfileBtn.html('Edit profile');
    }
});

$saveAddressBtn.click(() => {
    let id = $('#address-modal-id-input').val();
    let request = {
        regionId: $addressRegionSelect.val(),
        settlement: $addressModalSettlementInput.val(),
        streetName: $addressModalStreetNameInput.val(),
        streetNumber: $addressModalStreetNumberInput.val(),
        streetTypeId: $addressStreetTypeSelect.val()
    };
    if ($('#address-modal-id-input').val() === '') {
        $.ajax({
            url: `${API_URL}/address`,
            contentType: 'application/json',
            headers: {
                "Authorization": `Bearer ${window.localStorage.getItem('user_token')}`
            },
            type: 'post',
            data: JSON.stringify(request),
            success: function () {
                $addressModal.css('display', 'none');
                $addressesHolder.html('');
                addressesRequest($addressesHolder);
                clearAddressModal();
            }
        })
    } else {
        $.ajax({
            url: `${API_URL}/address?id=` + id,
            contentType: 'application/json',
            headers: {
                "Authorization": `Bearer ${window.localStorage.getItem('user_token')}`
            },
            type: 'put',
            data: JSON.stringify(request),
            success: function () {
                $addressModal.css('display', 'none');
                $addressesHolder.html('');
                addressesRequest($addressesHolder);
                clearAddressModal();
            }
        })
    }
});


function appendProfileInfoIntoContainer(response) {
    let link = response.user.image ? `http://localhost:8080/image/user_${response.user.id}/${response.user.image}` : 'https://media.istockphoto.com/vectors/small-house-icon-home-icon-vector-design-vector-id810190800?k=6&m=810190800&s=170667a&w=0&h=s2doFETLvYcb70M2_k_BOBaasGYrnuAuPfwArxJUK_Y=';
    $avatarHolder.append(`<img src="${link}" id="firm-avatar">`);
    $firmNameInput.val(response.user.name);
    $firmEmailInput.val(response.user.email);
    $firmPhoneInput.val(response.user.phone);
    $realtorsCount.append(`<label>${response.realtors} realtors</label>`);
    $realtiesCount.append(`<label>${response.realties} publications</label>`);
    $('#firm-id-input').val(response.firmId);
}

function regionRequest() {
    $.ajax({
        url: `${API_URL}/region/all`,
        contentType: 'application/json',
        type: 'get',
        success: function (response) {
            appendRegionsToSelect(response);
        }
    })
}

function appendRegionsToSelect(regions) {
    for (let region of regions) {
        $addressRegionSelect.append(` 
      <option value="${region.id}">${region.name}</option>
        `)
    }
}

function streetTypeRequest() {
    $.ajax({
        url: `${API_URL}/streettype/all`,
        contentType: 'application/json',
        type: 'get',
        success: function (response) {
            appendStreetsToSelect(response);
        }
    })
}

function appendStreetsToSelect(streets) {
    for (let street of streets) {
        $addressStreetTypeSelect.append(` 
      <option value="${street.id}">${street.name}</option>
        `)
    }
}

function clearAddressModal() {
    $('#address-modal-id-input').val('');
    $addressRegionSelect.val(0);
    $addressModalSettlementInput.val('');
    $addressModalStreetNameInput.val('');
    $addressModalStreetNumberInput.val('');
    $addressStreetTypeSelect.val(0);
}