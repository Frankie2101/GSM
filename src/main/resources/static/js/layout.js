/**
 * Executes when the DOM is fully loaded.
 */
window.addEventListener('DOMContentLoaded', event => {
    /**
     * Logic to toggle the sidebar's visibility.
     * It finds the toggle button and adds a click event listener
     * to toggle a CSS class on the body element.
     */
    const sidebarToggle = document.body.querySelector('#sidebarToggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', event => {
            event.preventDefault();
            document.body.classList.toggle('sb-sidenav-toggled');
        });
    }
});