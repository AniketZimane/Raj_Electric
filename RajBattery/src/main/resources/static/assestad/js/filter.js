function filterTbl() {
  var input, filter, table, tr, td, cell, i, j, hasVisibleRow = false;
  input = document.getElementById("tSearch");
  filter = input.value.toUpperCase();
  table = document.getElementById("dataTable1");
  tr = table.getElementsByTagName("tr");

  for (i = 1; i < tr.length; i++) {
    tr[i].style.display = "none"; // Hide the row initially.

    td = tr[i].getElementsByTagName("td");
    for (j = 0; j < td.length; j++) {
      cell = td[j];
      if (cell && cell.innerHTML.toUpperCase().indexOf(filter) > -1) {
        tr[i].style.display = ""; // Show the row if a match is found.
        hasVisibleRow = true;
        break;
      }
    }
  }

  // Display "No Records Found" message if no visible rows.
  var noRecords = document.getElementById("noRecordsMessage");
  if (!hasVisibleRow) {
    if (!noRecords) {
      noRecords = document.createElement("div");
      noRecords.id = "noRecordsMessage";
      noRecords.innerHTML = "No Records Found";
      table.parentNode.appendChild(noRecords);
    }
  } else {
    if (noRecords) {
      noRecords.parentNode.removeChild(noRecords);
    }
  }
}
