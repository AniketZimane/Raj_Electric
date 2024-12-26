// Handle hover and click interactions
const battery = document.querySelector('.big-battery');

// On mouse hover, apply hover effect
battery.addEventListener('mouseenter', () => {
  battery.classList.add('battery-hovered');
});

battery.addEventListener('mouseleave', () => {
  battery.classList.remove('battery-hovered');
});

// On mouse click, apply pulse animation
battery.addEventListener('click', () => {
  battery.classList.add('battery-clicked');
  setTimeout(() => {
    battery.classList.remove('battery-clicked');
  }, 300); // remove click animation after 0.3s
});

// Mouse follow animation
document.addEventListener('mousemove', (event) => {
  const batteryRect = battery.getBoundingClientRect();
  const mouseX = event.clientX;
  const mouseY = event.clientY;

  const centerX = batteryRect.left + batteryRect.width / 2;
  const centerY = batteryRect.top + batteryRect.height / 2;

  const deltaX = mouseX - centerX;
  const deltaY = mouseY - centerY;

  const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
  const maxDistance = 100; // reduced distance for a more subtle effect

  const movementX = (deltaX / distance) * maxDistance;
  const movementY = (deltaY / distance) * maxDistance;

  battery.style.transform = `translate(${movementX}px, ${movementY}px)`;
});

// Handle page redirection after charging is complete
const batteryLevel = document.querySelector('.battery-level');
const body = document.querySelector('body');

// Check when the charging animation is completed
batteryLevel.addEventListener('animationend', () => {
  // Wait 2 seconds after the animation ends before fading out
  setTimeout(() => {
    // Start fading out the entire page
    body.classList.add('fade-out');

    // Redirect to the next page after 2 seconds (while the fade-out is happening)
    setTimeout(() => {
      window.location.href = '/'; // Change this to your desired next page URL
    }, 2000); // Wait for fade-out to complete before redirecting
  }, 1000); // 2 seconds after animation completes
});
