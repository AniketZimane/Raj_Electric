document.addEventListener("DOMContentLoaded", function () {
                 new Splide('#splide', {
                     type   : 'loop',
                     perPage: 1,
                     autoplay: true,
                     interval: 3000,
                 }).mount();
             });

document.addEventListener("DOMContentLoaded",function(){
    const endDateInput = document.getElementById('endDate');
        const errorSpan = document.getElementById('expiry-error');

        endDateInput.addEventListener('input', () => {
            const selectedDate = new Date(endDateInput.value);
            const today = new Date();


            if (selectedDate < today) {
                errorSpan.textContent = "Please select a date that is today or later.";
            } else {
                errorSpan.textContent = "";
            }
        });
});

document.addEventListener("DOMContentLoaded", function () {
document.querySelectorAll("#readMore").forEach(function (button) {
        button.addEventListener("click", function () {
            var cardBody = button.closest(".card-body");

            var fullDescription = cardBody.querySelector("#fullDescription");

            if (fullDescription.style.display === "none") {
                fullDescription.style.display = "block"; // Show full description

                button.innerHTML = "Read less"; // Change link text
            } else {
                fullDescription.style.display = "none"; // Hide full description

                button.innerHTML = "Read more"; // Reset link text
            }
        });
    });
});

function filterProduct() {
    var input = document.getElementById("prodName");
    var filter = input.value.toUpperCase();


    var productDiv = document.getElementById("productDiv");


    var productCards = productDiv.getElementsByClassName("col-md-4");


    for (var i = 0; i < productCards.length; i++) {

        var titleElement = productCards[i].querySelector("h2");


        if (titleElement) {
            var titleText = titleElement.textContent || titleElement.innerText;

            if (titleText.toUpperCase().indexOf(filter) > -1) {

                productCards[i].style.display = "";
            } else {

                productCards[i].style.display = "none";
            }
        }
    }
}

