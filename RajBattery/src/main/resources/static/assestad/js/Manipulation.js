$(document).ready(function () {

    /* ================= GLOBAL STATE ================= */

    let itemCount = 0;
    let detailsFetched = false;


    /* ================= HELPERS ================= */

    function showToast(type, msg) {

        Swal.fire({
            toast: true,
            position: 'top-end',
            icon: type,
            title: msg,
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true
        });
    }


    function updateButtons() {

        // Fetch enabled only if items exist
        $('#fetchDetails').prop('disabled', itemCount === 0);


        // Invoice enabled only if:
        // items + fetch done + net amount exists
        const net = $('#netAmt').val();

        if (itemCount > 0 && detailsFetched && net && Number(net) > 0) {

            $('#invoice').removeClass('disabled');

        } else {

            $('#invoice').addClass('disabled');
        }
    }


    /* ================= INITIAL ================= */

    $('#invoice').addClass('disabled');
    $('#fetchDetails').prop('disabled', true);

    updateButtons();


    /* ================= CATEGORY → PRODUCTS ================= */

    $('#categoryId').on('change', function () {

        const catId = $(this).val();
        const productSelect = $('#productSelect');

        productSelect.empty().append('<option>Loading...</option>');

        axios.post('/admin/category/', { categoryId: catId })

            .then(res => {

                productSelect.empty()
                    .append('<option disabled selected>-- Select Product --</option>');

                res.data.forEach(p => {

                    productSelect.append(
                        `<option value="${p.pid}" data-rate="${p.prize}">
                            ${p.pname}
                        </option>`
                    );
                });

            })

            .catch(() => {

                productSelect.empty()
                    .append('<option>Error loading</option>');
            });

    });



    /* ================= PRODUCT CHANGE ================= */

    $('#productSelect').on('change', function () {

        const rate = $(this).find(':selected').data('rate') || 0;

        $('#rate').val(rate);

        if (Number(rate) === 0) {

            showToast('warning', 'Rate is 0. Please update product price.');

        }

        calculateTotal();

    });



    /* ================= QTY CHANGE ================= */

    $('#qtyValue').on('input', function () {

        calculateTotal();

    });


    function calculateTotal() {

        const qty = Number($('#qtyValue').val()) || 0;
        const rate = Number($('#rate').val()) || 0;

        const total = qty * rate;

        $('#totalAmt').val(total.toFixed(2));
    }



    /* ================= ADD ITEM ================= */

    $('#addItemButton').on('click', function () {

        const productId = $('#productSelect').val();
        const qty = $('#qtyValue').val();
        const rate = $('#rate').val();
        const total = $('#totalAmt').val();
        const catId = $('#categoryId').val();

        const user = $('#userName').val();
        const address = $('#userAddress').val();
        const series = $('#seriesNo').val();


        /* ===== VALIDATION ===== */

        if (!productId || !qty || !rate || !total || !catId ||
            !user || !address || !series) {

            showToast('info', 'Fill all fields');
            return;
        }


        if (qty <= 0) {

            showToast('warning', 'Quantity must be greater than 0');
            return;
        }


        if (Number(rate) === 0) {

            showToast('error', 'Product rate is 0. Update price first.');
            return;
        }


        /* ===== SEND ===== */

        const data = {

            productId,
            quantity: qty,
            rate,
            totalAmount: total,
            categoryId: catId,
            userName: user,
            userAddress: address,
            seriesNo: series
        };


        axios.post('/save-item/', data)

            .then(() => {

                showToast('success', 'Item added');

                itemCount++;
                detailsFetched = false;

                resetItemForm();

                updateButtons();
            })

            .catch(err => {

                showToast('info',
                    err.response?.data?.message || 'Failed'
                );
            });

    });



    function resetItemForm() {

        $('#productSelect').val('');
        $('#qtyValue').val('');
        $('#rate').val('');
        $('#totalAmt').val('');
        $('#seriesNo').val('');
    }



    /* ================= FETCH DETAILS ================= */

    $('#fetchDetails').on('click', function () {

        if (itemCount === 0) {

            showToast('info', 'No product added yet');
            return;
        }


        const user = $('#userName').val();

        if (!user) {

            showToast('info', 'Enter customer name');
            return;
        }


        axios.post('/fetch-details/', { userName: user })

            .then(res => {

                const list = res.data;


                if (!list || list.length === 0) {

                    showToast('info', 'No data found');
                    return;
                }


                const gross = list.reduce(
                    (sum, i) => sum + (i.prodTotalVal || 0),
                    0
                );


                $('#gross').val(gross.toFixed(2));
                $('#charges').val(28);
                $('#netAmt').val('');


                detailsFetched = true;

                showToast('success', 'Details fetched');

                updateButtons();

            })

            .catch(() => {

                showToast('error', 'Failed to fetch details');

            });

    });



    /* ================= DISCOUNT ================= */

    $('#discount').on('input', function () {

        const gross = Number($('#gross').val()) || 0;
        const charges = Number($('#charges').val()) || 0;
        const discount = Number($(this).val()) || 0;


        const gstAmt = (gross * charges) / 100;
        const discAmt = ((gross + gstAmt) * discount) / 100;

        const net = (gross + gstAmt) - discAmt;


        $('#netAmt').val(net.toFixed(2));

        updateButtons();

    });



    /* ================= PAYMENT STATUS ================= */

    $('#paymentStatus').on('change', function () {

        const status = $(this).val();


        if (status === 'half_payment') {

            $('#paidAmountGroup').show();

        } else {

            $('#paidAmountGroup').hide();
            $('#paidAmount').val('');

        }

    });



    /* ================= CREATE INVOICE ================= */

    $('#invoice').on('click', function () {


        if ($(this).hasClass('disabled')) {

            showToast('info', 'Complete billing first');
            return;
        }


        const payload = {

            userName: $('#userName').val(),
            emailId: $('#emailId').val(),
            mobNumber: $('#mobNumber').val(),
            vehicleNumber: $('#vehical').val(),

            gross: $('#gross').val(),
            gst: $('#charges').val(),
            discount: $('#discount').val(),

            paymentMode: $('#paymentMode').val(),
            paidAmount: $('#paidAmount').val(),
            paymentStatus: $('#paymentStatus').val()
        };


        axios.post('/create-invoice/', payload)

            .then(() => {

                const user = $('#userName').val();

                window.location.href =
                    `/admin/invoice/?userName=${encodeURIComponent(user)}`;

            })

            .catch(() => {

                showToast('error', 'Invoice creation failed');

            });

    });

});

