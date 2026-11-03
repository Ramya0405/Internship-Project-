/**
 * FarmChainX – Agricultural Traceability Platform
 * Global Frontend JavaScript Utilities
 */
document.addEventListener('DOMContentLoaded', function () {
    // ── Auto-dismiss flash alerts after 5 seconds ────────────────────────────
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            if (typeof bootstrap !== 'undefined' && bootstrap.Alert) {
                const bsAlert = bootstrap.Alert.getInstance(alert) || new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    });

    // ── Mobile sidebar toggle ────────────────────────────────────────────────
    const toggleBtn = document.getElementById('sidebarToggle');
    const sidebar   = document.querySelector('.fcx-sidebar');
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', function () {
            sidebar.classList.toggle('open');
        });
    }

    // ── Copy Tracking ID to Clipboard ────────────────────────────────────────
    document.querySelectorAll('.btn-copy-tracking').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const text = this.getAttribute('data-tracking-id');
            if (text && navigator.clipboard) {
                navigator.clipboard.writeText(text).then(() => {
                    const button = this;
                    const originalNodes = Array.from(button.childNodes, node => node.cloneNode(true));
                    const icon = document.createElement('i');
                    icon.className = 'fa fa-check text-success';
                    button.replaceChildren(icon, document.createTextNode(' Copied!'));
                    setTimeout(() => { button.replaceChildren(...originalNodes); }, 2000);
                });
            }
        });
    });
});
