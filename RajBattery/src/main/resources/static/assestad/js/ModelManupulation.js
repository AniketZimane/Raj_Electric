
    // Use jQuery to handle the click event
    $(document).ready(function() {
        // When the button is clicked
        $('.btnMaterial').on('click', function() {
            // Get the data-id value
            var invoiceId = $(this).data('id');
            // Set the value in the modal's input field
            $('#invoiceId').val(invoiceId);
            alert(invoiceId);
        });
    });
