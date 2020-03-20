const API_URL = 'http://localhost:8080';
const $imageInput = $('#image-upload')
const $roleRadio = $('input:radio')
const $registrationBtn = $('#registration-btn');
const $loginInput = $('#login-input');
const $phoneInput = $('#phone-input');
const $passwordInput = $('#password-input');
const $emailInput = $('#email-input');
const $nameInput = $('#name-input');
// const $settlementInput = $('#settlement-input');
// const $streetNameInput = $('#street-name-input');
// const $streetNumberInput = $('#street-number-input');
// const $firmRegionSelect = $('#firm-region-select');
// const $firmStreetTypeSelect = $('#firm-street-type-select');

function appendStreetTypesToSelect(streettypes) {
    for (let street of streettypes){
        $('#firm-street-type-select').append(`
          <option class="street-option" value="${street.id}">${street.name}</option>
        `)
    }
}

$(document).ready(() => {
   loadRegions($('#firm-region-select'));
   loadRegions($('#realtor-region-select'));
    $.ajax({
        url: `${API_URL}/streettype/all`,
        type: 'get',
        success: function(response) {
            appendStreetTypesToSelect(response);
        }
    });
});

$registrationBtn.click(() => {
    let user = {
        name: $nameInput.val(),
        login: $loginInput.val(),
        password: $passwordInput.val(),
        email: $emailInput.val(),
        phoneNumber : $phoneInput.val()
    }
    getBase64FromFile($('#image-input')[0].files[0])
        .then(image => user.image = image)
        .catch(image => user.image = null)
        .finally( () => {
            let role  = $('input[name="user-role"]:checked').val();
            console.log(role);
            console.log(user);
            if (role === 'realtor') registerRealtor(user);
            else if(role === 'user') registerUser(user);
            else registerFirm(user);
            }
        );
});

function registerFirm(user) {
    let request = {
        user: user,
    };
    $.ajax({
        url: `${API_URL}/firm/register`,
        type: 'post',
        contentType: 'application/json',
        data: JSON.stringify(request),
        success: function(response) {
            window.localStorage.setItem('user_id', response.id);
            window.localStorage.setItem('user_name', response.username);
            window.localStorage.setItem('user_token', response.token);
            window.location.href = `${API_URL}/firm-profile`
        },
        error: function(xhr, status, error){
            let errorMessage = xhr.status + ': ' + xhr.statusText
            alert('Error - ' + errorMessage);
        }
    });
}

function registerUser(request) {
    $.ajax({
        url: `${API_URL}/user/register`,
        type: 'post',
        contentType: 'application/json',
        data: JSON.stringify(request),
        success: function(response) {
            alert('ok')
            window.localStorage.setItem('user_id', response.id);
            window.localStorage.setItem('user_name', response.username);
            window.localStorage.setItem('user_token', response.token);
        },
        error: function(xhr, status, error){
            let errorMessage = xhr.status + ': ' + xhr.statusText;
            console.log(xhr);
            console.log('Error - ' + errorMessage);
            console.log(status);
            console.log(error);
            console.log('Error - ' + errorMessage);

        }
    });
}

function registerRealtor(user) {
    let request = {
        user: user,
        regionId : $('#realtor-region-select').val()
    }
    $.ajax({
        url: `${API_URL}/realtor/register`,
        type: 'post',
        contentType: 'application/json',
        data: JSON.stringify(request),
        success: function(response) {
            alert('ok')
        },
        error: function(xhr, status, error){
            let errorMessage = xhr.status + ': ' + xhr.statusText
            alert('Error - ' + errorMessage);
        }
    });
}

function readURL(input) {
    $('#image').remove();
    if (input.files && input.files[0]) {
        let reader = new FileReader();
        reader.onload = function (e) {
            $imageInput.prepend(`<img  alt="" id="image">`);
            $('#image')
                .attr('src', e.target.result);
        };

        reader.readAsDataURL(input.files[0]);
    }
}

function loadRegions(selector){
    $.ajax({
        url: `${API_URL}/region/all`,
        type: 'get',
        success: function(response) {
            appendRegionsToSelect(selector,response);
        }
    });
}

function appendRegionToSelect(selector,region){
    selector.append(`
        <option class="region-option" value="${region.id}">${region.name}</option>
    `)
}

function appendRegionsToSelect(selector, regions){
    for (let region of regions){
        appendRegionToSelect(selector,region);
    }
}

$roleRadio.click((e) =>{
    let role  = $(e.target).val();
    console.log(role);
    if (role === 'realtor') actionOnRealtorRadio();
    else if(role === 'user') actionOnUserRadio();
    else actionOnFirmRadio();

});

function actionOnRealtorRadio() {
    $('#realtor-location').css('display', 'block');
    $('#firm-location').css('display', 'none');
}

function actionOnFirmRadio() {
    $('#realtor-location').css('display', 'none');
    $('#firm-location').css('display', 'block');
}

function actionOnUserRadio() {
    $('#realtor-location').css('display', 'none');
    $('#firm-location').css('display', 'none');
}

const getBase64FromFile = (file) => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
};