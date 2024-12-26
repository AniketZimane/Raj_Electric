document.addEventListener("DOMContentLoaded", function() {
    // Bind the submit event for the form
    document.getElementById("paidAmountForm").addEventListener("submit", function(event) {
        // Get the current values of the inputs
        const paidAmount = parseFloat(document.querySelector("[name='paidAmount']").value);
        const netAmount = parseFloat(document.querySelector("[name='netAmount']").value);
        const balance = parseFloat(document.querySelector("[name='balance']").value);

        // Calculate the total of paidAmount and balance
        const totalPaid = paidAmount + balance;

        // Log for debugging
        console.log("Paid Amount: ", paidAmount);
        console.log("Net Amount: ", netAmount);
        console.log("Balance: ", balance);
        console.log("Total Paid: ", totalPaid);

        // If the total paid amount exceeds the net amount, prevent form submission and show an error
        if (totalPaid > netAmount) {
            event.preventDefault(); // Prevent form submission
            swal("Oops!", "The total paid amount (Paid Amount + Balance) must not exceed the Net Amount of: " + netAmount, "error");
        }
    });

    // Assign data attributes to the modal form fields dynamically
    const modalLinks = document.querySelectorAll('.btn[data-bs-toggle="modal"]');
    modalLinks.forEach(link => {
        link.addEventListener("click", function() {
            const netAmount = parseFloat(link.getAttribute('data-netAmount'));
            const paidAmount = parseFloat(link.getAttribute('data-paidAmount'));

            // Set these values in the modal form fields
            document.querySelector("#netAmount").value = netAmount;
            document.querySelector("#paidAmount").value = paidAmount;
            document.querySelector("#balance").value = paidAmount; // Assuming balance is same as paid amount
        });
    });
});
