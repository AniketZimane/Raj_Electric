document.addEventListener("DOMContentLoaded", function() {
        var button = document.getElementById("printBtn");
        button.addEventListener("click", function() {
          var element = document.getElementById("printContent");
          html2pdf()
            .from(element)
            .save("invoice.pdf");
        });
      });