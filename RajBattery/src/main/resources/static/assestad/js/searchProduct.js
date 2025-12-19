$(document).ready(function () {
    if (typeof $.fn.select2 === 'undefined') {
        console.error("Select2 not loaded properly!");
        return;
    }

    $('#productSelect').select2({
        placeholder: "Search for a product",
        ajax: {
            url: '/api/products/search',
            dataType: 'json',
            delay: 250,
            data: function (params) {
                return { query: params.term };
            },
            processResults: function (data) {
                return {
                    results: data.map(p => ({
                        id: p.id,
                        text: p.name
                    }))
                };
            }
        },
        minimumInputLength: 1,
        width: '100%'
    });
});
