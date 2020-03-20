const API_URL = 'http://localhost:8080';
const $createAccountBtn = $('#create-account-btn');
const $loginBtn = $('#login-btn');
const $loginInput = $('#login-input');
const $passwordInput = $('#password-input');

$createAccountBtn.click(()=>{
        window.location.href= `${API_URL}/registration-page`;
});

$loginInput.keypress(() => {
    $('#login-input:not(:placeholder-shown)').css('border', '2px solid #3d88fe');
    $('#login-input:not(:placeholder-shown)').css('box-shadow', 'none');
    $('#password-input:not(:placeholder-shown)').css('box-shadow', 'none');
    $('#password-input:not(:placeholder-shown)').css('border', '2px solid #3d88fe');
    $('#label-container .error-label').remove();
});

$loginBtn.click(() => {
    let request = {
        login: $loginInput.val(),
        password: $passwordInput.val()
    };
    $.ajax({
        url: `${API_URL}/user/login`,
        type: 'post',
        contentType: 'application/json',
        data: JSON.stringify(request),
        success: function(response) {
            window.localStorage.setItem('user_id', response.id);
            window.localStorage.setItem('user_name', response.username);
            window.localStorage.setItem('user_token', response.token);
            window.location.href = `${API_URL}/firm-profile`
        },
        error: function (xhr, status, error) {
            let responseText = jQuery.parseJSON(xhr.responseText);
            if (responseText.message === 'Access Denied'){
                $('#login-input:not(:placeholder-shown)').css('border', '2px solid #fe3d0a');
                $('#login-input:not(:placeholder-shown)').css('box-shadow', '0 0 5px 1px #fe3d0a');
                $('#password-input:not(:placeholder-shown)').css('box-shadow', '0 0 5px 1px #fe3d0a');
                $('#password-input:not(:placeholder-shown)').css('border', '2px solid #fe3d0a');
                $('#label-container').append(`<label class="error-label">Login or password is not correct, try again</label>`)
                // console.log('of');
            }
            console.log(xhr.status);
            console.log(xhr.statusText);
            console.log(responseText.ExceptionType);
            console.log(responseText.stackTrace);
            console.log(responseText.message);
            // console.log();
        }
    });
});