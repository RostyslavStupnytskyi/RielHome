const API_URL = 'http://localhost:8080';
const $openBtn = $('#open-btn');
const $createBtn = $('#create-btn');
const $tableBody = $('#table-body');
const $modal = $('#modal-create');
const $modalAlert = $('#modal-alert');
const $deleteBtn = $('#delete-btn');
const $pageBtnContainer = $('#page-buttons-container');
const $contentContainer = $('#content-container');
let totalPages;
let currentPage;

$(document).ready(() => {
    currentPage = 0;
    makeRequest();
});

function makeRequest() {
    $.ajax({
        url: `${API_URL}/streettype?page=${currentPage}&size=20`,
        type: 'get',
        success: function(response) {
            $pageBtnContainer.html('');
            $tableBody.html('');
            totalPages = response.totalPages;
            appendStreetTypesToTable(response.data);
            actionsOnUpdateBtn();
            actionsOnDeleteButton();
            appendPageButtons();
            actionsOnPageBtn();
            findCurrentButton();
        }
    });
}

function appendStreetTypeToTable(streettype) {
    $tableBody.append(` 
            <tr class="table-component">
                <th class="id">${streettype.id}</th>
                <th class="name"> <input type="text" class="table-input" value="${streettype.name}" disabled></th>
                <th class="actions"> <button value="${streettype.id}" class="update-btn btn">Update</button> <button value="${streettype.id}" class="delete-btn btn">Delete</button></th>
            </tr>
    `)
}

function appendPageButtons(){
    // for (let i = 0; i < totalPages; i++){
    if (totalPages <= 24) {
        for (let i = 0; i < totalPages; i++) {
            $pageBtnContainer.append(`
        <button value="${i}" class="page-btn">${i + 1}</button>
        `);
        }
    } else {
        if (currentPage > 12){
            for (let i = +currentPage - 12; i < totalPages; i++) {
                $pageBtnContainer.append(`
        <button value="${i}" class="page-btn">${i + 1}</button>
        `);
            }
            if (currentPage + 12 < totalPages) $pageBtnContainer.append(`...`);
        } else {
            for (let i = 0; i < 24; i++) {
                $pageBtnContainer.append(`
        <button value="${i}" class="page-btn">${i + 1}</button>
        `);
            }
            $pageBtnContainer.append(`...`);
        }
    }
};

function actionsOnPageBtn(){
    $('.page-btn').click((e)=>{
        let $btn = $(e.target);
        currentPage = $btn.val();
        console.log(currentPage);
        makeRequest();
    });
}


function findCurrentButton() {
    $('.page-btn').each((i,e)=>{
        let $btn = $(e);
        if ($btn.val() === currentPage){
            $btn.addClass('current-page-btn');
        }
    });
}

$openBtn.click(()=>{
    // console.log('aa')
    $modal.css('display','block');
});

function appendStreetTypesToTable(streettypes){
    for (let streettype of streettypes){
        appendStreetTypeToTable(streettype);
    }
}

$createBtn.click(()=>{
    let $nameInput = $('#name-create');
    let request = {
        name: $nameInput.val()
    }
    $.ajax({
        url: `${API_URL}/streettype`,
        type: 'put',
        contentType: 'application/json',
        data: JSON.stringify(request),
        success: function() {
            window.location.reload();
        }
    });
});

function actionsOnDeleteButton(){
    $('.delete-btn').click((e)=> {
        let $btn = $(e.target);
        let id = $btn.val();
        $modalAlert.css('display','block');
        $deleteBtn.val(id);

    });
}

$deleteBtn.click((e) => {
    let $btn = $(e.target);
    let id = $btn.val();
    $.ajax({
        url: `${API_URL}/streettype?id=${id}`,
        type: 'delete',
        contentType: 'application/json',
        success: function() {
            makeRequest();
            $modalAlert.css('display','none');
            }
    });
});

function actionsOnUpdateBtn() {
    $('.update-btn').click((e) => {
        let $btn = $(e.target);
        let id = $btn.val();
        let input = $btn.parent().siblings().children('.table-input');

        if (input.prop('disabled')) {
            input.attr('disabled', false);
            $btn.html('Confirm');
        } else {
            input.attr('disabled', true);
            let request = {
                name: input.val()
            };
            $.ajax({
                url: `${API_URL}/streettype?id=${id}`,
                type: 'post',
                contentType: 'application/json',
                data: JSON.stringify(request),
                success: function() {
                }
            });

            $btn.html('Update');
        }
    });
}