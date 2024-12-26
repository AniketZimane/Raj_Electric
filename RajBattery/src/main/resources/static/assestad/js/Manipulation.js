
$(document).ready(function () {

function renderInhouseInvoices(invoices) {
    const tableBody = document.getElementById('invoicesTableBody'); // Your table body ID
    tableBody.innerHTML = ''; // Clear any existing rows

    invoices.forEach(invoice => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${invoice.id}</td>
            <td>${invoice.prodId}</td>
            <td>${invoice.prodId}</td>
            <td>${invoice.prodQty}</td>
            <td>${invoice.prodRate}</td>
            <td>${invoice.prodTotalVal}</td>
//            <td class="text-center">
//                <a class="btn btn-info" type="button" th:href="@{/admin/view/invoice/?orderId={orderId}(orderId=${data.id})}">
//                    <i class="fa fa-eye"></i>
//                </a>
//                <a class="btn btn-danger" type="button" th:href="@{/admin/delete/invoice/{id}/(id = ${data.id})}" style="margin: 10px;"><svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" fill="currentColor" viewBox="0 0 16 16" class="bi bi-trash-fill">
//                        <path d="M2.5 1a1 1 0 0 0-1 1v1a1 1 0 0 0 1 1H3v9a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V4h.5a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H10a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1H2.5zm3 4a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 .5-.5zM8 5a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-1 0v-7A.5.5 0 0 1 8 5zm3 .5v7a.5.5 0 0 1-1 0v-7a.5.5 0 0 1 1 0z"></path></svg></a>
//            </td>

        `;
        tableBody.appendChild(row);
    });
}


    $('#invoice').addClass("disabled");
    var categoryDropdown = $('#categoryId');
    var productDropdown = $('#productSelect');
    var rateInput = $('#rate');
    var totalAmtInput = $('#totalAmt');
    var qtyInput = $('#qtyValue');  // Quantity input field
    var maxQty = 0;  // Stores the max quantity of the selected product

        // When the category is changed
        categoryDropdown.on('change', function () {
            console.log("change");
            var selectedCategoryId = categoryDropdown.val(); // Get selected category ID
            console.log('Selected Category ID:', selectedCategoryId);

            // Endpoint for fetching products
            var endpoint = '/admin/category/';

            // Clear the product dropdown and show a loading message
            productDropdown.empty().append('<option disabled selected>Loading...</option>');

            // Fetch products based on the selected category
            axios.post(endpoint, { categoryId: selectedCategoryId })
                .then(function (response) {
                    var productList = response.data; // Array of products
                    console.log('Fetched Product List:', productList);

                    // Reset the product dropdown
                    productDropdown.empty().append('<option value="" disabled selected>-- Select Product --</option>');

                    // Populate the product dropdown
                    productList.forEach(function (productList) {
                        productDropdown.append(
                            `<option value="${productList.pid}" data-rate="${productList.prize}">${productList.pname}</option>`
                        );
                    });
                })
                .catch(function (error) {
                    console.error('Error fetching products:', error);
                    productDropdown.empty().append('<option value="" disabled selected>Error loading products</option>');
                });
        });

        // When a product is selected
        productDropdown.on('change', function () {
            var selectedProduct = productDropdown.find('option:selected'); // Get selected product

            // Retrieve rate and quantity from the selected product
            var rate = selectedProduct.data('rate');
//            maxQty = selectedProduct.data('qty'); // Store the max quantity

            // Update rate and max quantity
            rateInput.val(rate);
//            qtyInput.attr('max', maxQty);

            // Calculate the total amount (qty * rate)
            var qty = qtyInput.val();
            var totalAmt = qty * rate;
            totalAmtInput.val(totalAmt.toFixed(2)); // Show total amount with 2 decimal places
        });

        // When the quantity changes
        qtyInput.on('input', function () {
            var qtyValue = $(this).val(); // Quantity entered by the user
            var rate = rateInput.val();   // Current rate

            // Ensure the entered quantity doesn't exceed the max quantity
//            if (qtyValue > maxQty) {
//                alert(`You can only order up to ${maxQty} units of this product.`);
//                $(this).val(maxQty);
//                qtyValue = maxQty;
//            }

            // Update the total amount
            var totalAmt = qtyValue * rate;
            totalAmtInput.val(totalAmt.toFixed(2));
        });

        // Clear inputs when delete button is clicked
        $('.delete-row').on('click', function () {
            // Clear inputs but maintain disabled state for certain fields
            $('input[type="text"], input[type="number"], select').val('');
            productDropdown.empty().append('<option value="" disabled selected>-- Select Product --</option>');
            rateInput.val('').prop('disabled', true);
            totalAmtInput.val('').prop('disabled', true);
        });

        // Add item to the database when the add button is clicked
        $('#addItemButton').on('click', function () {
            // Retrieve input values
            var productId = productDropdown.val();
            var qty = qtyInput.val();
            var catId = categoryDropdown.val();
            var rate = rateInput.val();
            var totalAmt = totalAmtInput.val();
            var userName = $('#userName').val();
            var userAddress = $('#userAddress').val();

            // Validate required fields
            if (!productId || !qty || !rate || !totalAmt || !catId || !userName || !userAddress) {
                alert('Please fill out all required fields.');
                return;
            }

            // Prepare data for submission
            var data = {
                productId: productId,
                quantity: qty,
                rate: rate,
                totalAmount: totalAmt,
                categoryId: catId,
                userName: userName,
                userAddress: userAddress
            };

            // Send data to the backend
            axios.post('/save-item/', data)
                .then(function (response) {
                    swal({
                        icon: "success",
                        text: "Item added successfully"
                    });
//                    const data = response.data.inhouseInvoices; // Access the invoices
//                    renderInhouseInvoices(data);
                    // Clear form fields
                    productDropdown.val('');
                    qtyInput.val('');
                    rateInput.val('').prop('disabled', true);
                    totalAmtInput.val('').prop('disabled', true);
                })
                .catch(function (error) {
                    swal({
                            icon: "info",
                            text: "You Can't sell more then actual stock"
                        });
                });
            });
    //fetch details
    $('#fetchDetails').on('click', function () {

            var userName = $('#userName').val();
            var gross = $('#gross');
            var netAmt = $('#netAmt');

            if (!userName) {
                swal({
                    icon: "info",
                    text: "Please provide a username."
                });
                return;
            }

            var data = { userName: userName };

            axios.post('/fetch-details/', data)
                .then(function (response) {
                    let orderData = response.data;
                    console.log('Response:', orderData);

                    if (orderData.length === 0) {
                        swal({
                            icon: "info",
                            text: "No orders found for the given username."
                        });
                        return;
                    }

                    let totalGrossAmount = orderData.reduce((sum, item) => sum + (item.prodTotalVal || 0), 0);
                    gross.val(totalGrossAmount.toFixed(2)).prop('disabled', true);
                    netAmt.val('').prop('disabled', true);
                    $("#charges").val(28);

                    swal({
                        icon: "success",
                        text: "Gross amount calculated and assigned successfully."
                    });
                })
                .catch(function (error) {
                    console.error('Error fetching details:', error);

                    swal({
                        icon: "error",
                        text: "Failed to fetch details. Please try again."
                    });
                });
        });

       $(document).ready(function () {
        // When the discount field changes
        $('#discount').on('input', function () {
            // Retrieve the values for gross amount, charges, and discount
            var grossAmt = parseFloat($('#gross').val()) || 0;  // Get the gross amount, default to 0 if invalid
            var charges = parseFloat($('#charges').val()) || 0;    // Get charges, default to 0 if invalid
            var discount = parseFloat($(this).val()) || 0;          // Get the discount, default to 0 if invalid

            // Calculate the deduction
            var mrpiWithCharges=(grossAmt*charges)/100;
            var discountAmt=((grossAmt+mrpiWithCharges)*discount)/100;

            // Calculate the net amount
            var netAmt = (grossAmt + mrpiWithCharges) -discountAmt;

            // Update the net amount field
            $('#netAmt').val(netAmt.toFixed(2));  // Show net amount with 2 decimal places
        });
    });
    $(document).ready(function () {
        // When the payment status changes
        $('#paymentStatus').on('change', function () {
            var paymentStatus = $(this).val();  // Get the selected payment status

            // Check if the selected payment status is 'half_payment'
            if (paymentStatus === 'half_payment')
            {
                $('#paidAmountGroup').show();
                $('#paidAmountGroup').css('display', 'block !important');
                $('#invoice').removeClass("disabled");

            }
            if (paymentStatus === 'unpaid')
            {
                $('#paidAmountGroup').hide();
                $('#invoice').addClass("disabled");
            }
            if (paymentStatus === 'paid')
            {
                $('#paidAmountGroup').hide();
                $('#paidAmount').val('');
                $('#invoice').removeClass("disabled");
            }
            else {
                $('#paidAmount').val(''); // Clear the paid amount field when not half payment
            }
        });
    });


    $('#invoice').on('click', function () {
    axios.post('/create-invoice/', {
        userName: $('#userName').val(),
        emailId: $('#emailId').val(),
        mobNumber: $('#mobNumber').val(),
        gross: $('#gross').val(),
        gst: $('#charges').val(),
        discount: $('#discount').val(),
        paymentMode: $('#paymentMode').val(),
        paidAmount: $('#paidAmount').val(),
        paymentStatus: $('#paymentStatus').val()
    }, {
        headers: { 'Content-Type': 'application/json' }
    })
    .then(function (response) {
        var userName = document.getElementById('userName').value;
        console.log('Response:', userName);
        window.location.href = `/admin/invoice/?userName=${encodeURIComponent(userName)}`;
//        return "redirect:/admin/invoice/?userName=" + URLEncoder.encode(userName, StandardCharsets.UTF_8);
    })
    .catch(function (error) {
        swal({
            icon: "error",
            text: "Failed to create invoice. Please try again."
        });
    });

    });
});
$(document).ready(function () {
    var categoryDropdown = $('#prodCategory');
    var productDropdown = $('#productId'); // Make sure to define the product dropdown

    categoryDropdown.on('change', function () {
        console.log("change");
        var selectedCategoryId = categoryDropdown.val(); // Get selected category ID
        console.log('Selected Category ID:', selectedCategoryId);

        // Endpoint for fetching products
        var endpoint = '/admin/category/';

        // Clear the product dropdown and show a loading message
        productDropdown.empty().append('<option disabled selected>Loading...</option>');

        // Fetch products based on the selected category
        axios.post(endpoint, { categoryId: selectedCategoryId })
            .then(function (response) {
                var productList = response.data; // Array of products
                console.log('Fetched Product List:', productList);

                // Reset the product dropdown
                productDropdown.empty().append('<option value="" disabled selected>-- Select Product --</option>');

                // Populate the product dropdown
                productList.forEach(function (product) { // Changed variable name for clarity
                    productDropdown.append(
                        `<option value="${product.pid}">${product.pname}</option>`
                    );
                });
            })
            .catch(function (error) {
                console.error('Error fetching products:', error);
                productDropdown.empty().append('<option value="" disabled selected>Error loading products</option>');
            });
    });
});
$(document).ready(function () {
    // Handle refresh link click
    $('#refreshLink').click(function (event) {
        event.preventDefault();

        // Get the userName value from the input field
        const userName = $('#userName').val();

        // Ensure the userName is not empty
        if (!userName) {
            alert('Please enter a valid customer name.');
            return;
        }

        // Make an AJAX request to fetch invoice items
        $.ajax({
            url: '/admin/get/invoice/items/',
            method: 'GET',
            data: { userName: userName },
            success: function (response) {
                // Clear existing table rows
                $('#invoicesTableBody').empty();

                // Check if response has data
                if (response && response.length > 0) {
                    response.forEach((item, index) => {
                        const row = `
                            <tr id="row-${item.id}">
                                <td class="text-center w-3x pt-2">${index + 1}</td>
                                <td>${item.prodId || ''}</td>
                                <td class="w-10x">${item.prodQty || ''}</td>
                                <td class="w-10x">${item.prodRate || ''}</td>
                                <td class="w-10x">${item.prodTotalVal || ''}</td>
                                <td class="w-10x">
                                    <button class="btn btn-sm btn-danger deleteItemBtn" data-id="${item.id}">Delete</button>
                                </td>
                            </tr>
                        `;
                        $('#invoicesTableBody').append(row);
                    });
                } else {
                    // Show message when no data is available
                    const emptyRow = `
                        <tr>
                            <td colspan="7" class="text-center">No data available</td>
                        </tr>
                    `;
                    $('#invoicesTableBody').append(emptyRow);
                }
            },
            error: function () {
                alert('Failed to fetch invoice items. Please try again later.');
            }
        });
    });

    // Add event listener for delete buttons using event delegation
    $('#invoicesTableBody').on('click', '.deleteItemBtn', function () {
        const itemId = $(this).data('id');
        const rowId = `#row-${itemId}`;

        axios
            .delete(`/admin/delete/invoice/item/${itemId}`)
            .then(response => {
                if (response.status === 200) {
                    // Remove the row from the table
                    $(rowId).remove();
                    alert('Item deleted successfully!');
                } else {
                    alert('Failed to delete item. Please try again.');
                }
            })
            .catch(error => {
                console.error('Error deleting item:', error);
                alert('An error occurred while deleting the item.');
            });
    });
});



