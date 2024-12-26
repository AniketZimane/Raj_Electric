$(document).ready(function () {



    // Render invoices table
    function renderInhouseInvoices(invoices) {
        const tableBody = $('#invoicesTableBody');
        tableBody.empty();

        if (invoices.length === 0) {
            tableBody.append('<tr><td colspan="7" class="text-center">No data available</td></tr>');
        } else {
            invoices.forEach(invoice => {
                tableBody.append(`
                    <tr>
                        <td>${invoice.id}</td>
                        <td>${invoice.prodId}</td>
                        <td>${invoice.prodQty}</td>
                        <td>${invoice.prodRate}</td>
                        <td>${invoice.prodTotalVal}</td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-danger deleteItemBtn" data-id="${invoice.id}">Delete</button>
                        </td>
                    </tr>
                `);
            });
        }
    }

    // Category and product dropdown
    $('#categoryId').on('change', function () {
        const categoryId = $(this).val();
        const productDropdown = $('#productSelect').empty().append('<option>Loading...</option>');

        axios.post('/admin/category/', { categoryId })
            .then(response => {
                const productList = response.data;
                productDropdown.empty().append('<option disabled selected>-- Select Product --</option>');
                productList.forEach(product => {
                    productDropdown.append(`<option value="${product.pid}" data-rate="${product.prize}">${product.pname}</option>`);
                });
            })
            .catch(() => {
                productDropdown.empty().append('<option disabled selected>Error loading products</option>');
            });
    });

    // Product selection changes
    $('#productSelect').on('change', function () {
        const selectedProduct = $(this).find('option:selected');
        const rate = selectedProduct.data('rate') || 0;

        $('#rate').val(rate);
        updateTotalAmount();
    });

    // Update total amount
    function updateTotalAmount() {
        const qty = parseFloat($('#qtyValue').val()) || 0;
        const rate = parseFloat($('#rate').val()) || 0;
        $('#totalAmt').val((qty * rate).toFixed(2));
    }

    $('#qtyValue').on('input', updateTotalAmount);

    // Add item to database
    $('#addItemButton').on('click', function () {
        const data = {
            productId: $('#productSelect').val(),
            quantity: $('#qtyValue').val(),
            rate: $('#rate').val(),
            totalAmount: $('#totalAmt').val(),
            categoryId: $('#categoryId').val(),
            userName: $('#userName').val(),
            userAddress: $('#userAddress').val(),
            seriesNo: $('#seriesNo').val()
        };

        if (Object.values(data).some(value => !value)) {
            alert('Please fill out all required fields.');
            return;
        }
        if(quantity==0)
        {
            alert('Quantity should be greater then zero');
            return;
        }

        axios.post('/save-item-black/', data)
            .then(() => {
                swal({ icon: "success", text: "Item added successfully" });
                $('qtyValue').val('');
                $('#seriesNo').val(''); // Fixed here
                $('#rate').val('').prop('disabled', true);
                $('#totalAmt').val('').prop('disabled', true);

            })
            .catch(err => {
                swal({ icon: "info", text: err.response?.data?.message || "Error saving item." });
            });
    });

    // Fetch details
    $('#fetchDetails').on('click', function () {
        const userName = $('#userName').val();

        if (!userName) {
            swal({ icon: "info", text: "Please provide a username." });
            return;
        }

        axios.post('/fetch-details/', { userName })
            .then(response => {
                const orderData = response.data;
                const totalGrossAmount = orderData.reduce((sum, item) => sum + (item.prodTotalVal || 0), 0);

                $('#gross').val(totalGrossAmount.toFixed(2)).prop('disabled', true);
                $('#charges').val(28);
                swal({ icon: "success", text: "Gross amount calculated." });
            })
            .catch(() => {
                swal({ icon: "error", text: "Failed to fetch details. Please try again." });
            });
    });

    // Delete item
    $('#invoicesTableBody').on('click', '.deleteItemBtn', function () {
        const itemId = $(this).data('id');

        axios.delete(`/admin/delete/invoice/item/${itemId}`)
            .then(() => {
                $(`#row-${itemId}`).remove();
                swal({ icon: "success", text: "Item deleted successfully." });
            })
            .catch(() => {
                swal({ icon: "error", text: "Error deleting item." });
            });
    });

    // Calculate net amount with discount
    $('#discount').on('input', function () {
        const grossAmt = parseFloat($('#gross').val()) || 0;
        const charges = parseFloat($('#charges').val()) || 0;
        const discount = parseFloat($(this).val()) || 0;

        const mrpiWithCharges = (grossAmt * charges) / 100;
        const discountAmt = ((grossAmt + mrpiWithCharges) * discount) / 100;
        const netAmt = grossAmt + mrpiWithCharges - discountAmt;

        $('#netAmt').val(netAmt.toFixed(2));
    });

    // Payment status change
    $('#paymentStatus').on('change', function () {
        const status = $(this).val();
        $('#paidAmountGroup').toggle(status === 'half_payment');
        $('#invoice').toggleClass('disabled', status === 'unpaid');
        if (status === 'paid') $('#paidAmount').val('');
    });

    // Create invoice
    $('#invoice').on('click', function () {
        const data = {
            userName: $('#userName').val(),
            emailId: $('#emailId').val(),
            mobNumber: $('#mobNumber').val(),
            gross: $('#gross').val(),
            gst: $('#charges').val(),
            discount: $('#discount').val(),
            paymentMode: $('#paymentMode').val(),
            paidAmount: $('#paidAmount').val(),
            paymentStatus: $('#paymentStatus').val()
            vehicleNumber: $('#vehicleNumber').val()
        };

        axios.post('/create-invoice-black/', data)
            .then(() => {
                const userName = encodeURIComponent($('#userName').val());
                window.location.href = `/admin/invoice/black/?userName=${userName}`;
            })
            .catch(() => {
                swal({ icon: "error", text: "Failed to create invoice. Please try again." });
            });
    });

    // Refresh invoice items
    $('#refreshLink').on('click', function (e) {
        e.preventDefault();
        const userName = $('#userName').val();

        if (!userName) {
            alert('Please enter a valid customer name.');
            return;
        }

        axios.get('/admin/get/invoice/items/', { params: { userName } })
            .then(response => renderInhouseInvoices(response.data))
            .catch(() => alert('Failed to fetch invoice items. Please try again.'));
    });
});
