function updateClock() {
    var now = new Date();
    var time = now.toLocaleTimeString();
    document.getElementById('clock').textContent = time;
}

// Update the clock every second
setInterval(updateClock, 1000);
