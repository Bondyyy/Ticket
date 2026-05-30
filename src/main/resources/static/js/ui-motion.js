(function () {
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

    if (reduceMotion.matches) {
        document.documentElement.classList.add('reduced-motion');
        return;
    }

    document.documentElement.classList.add('ui-motion-ready');

    const revealSelector = [
        '.landing-page > section',
        '.events-page .section-heading',
        '.events-toolbar',
        '.event-card',
        '.empty-events',
        '.event-header',
        '.booking-page .main-col',
        '.booking-page .side-col',
        '.checkout-card',
        '.ticket-container .glass-header',
        '.ticket-card',
        '.detail-container > *',
        '.orders-container .glass-header',
        '.order-group-card',
        '.scan-header',
        '.scan-alert:not([hidden])',
        '.scan-card',
        '.history-card',
        '.dashboard-page .filter-card',
        '.dashboard-page .stat-card',
        '.dashboard-page .chart-card',
        '.dashboard-page .table-card'
    ].join(',');

    const revealItems = Array.from(document.querySelectorAll(revealSelector));

    revealItems.forEach((item, index) => {
        item.classList.add('reveal-on-scroll');
        if (!item.style.getPropertyValue('--reveal-delay')) {
            item.style.setProperty('--reveal-delay', `${Math.min((index % 8) * 60, 420)}ms`);
        }
    });

    if ('IntersectionObserver' in window) {
        const revealObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (!entry.isIntersecting) return;
                entry.target.classList.add('is-visible');
                observer.unobserve(entry.target);
            });
        }, { threshold: 0.12, rootMargin: '0px 0px -8% 0px' });

        revealItems.forEach(item => revealObserver.observe(item));
    } else {
        revealItems.forEach(item => item.classList.add('is-visible'));
    }

    const canSpotlight = window.matchMedia('(hover: hover) and (pointer: fine)').matches;
    if (canSpotlight) {
        const spotlightItems = document.querySelectorAll([
            '.ui-hover-card',
            '.feature-card',
            '.event-card',
            '.dashboard-showcase',
            '.ticket-card',
            '.ticket-pass',
            '.scan-card',
            '.dashboard-page .stat-card',
            '.dashboard-page .chart-card'
        ].join(','));

        spotlightItems.forEach(card => {
            card.classList.add('ui-spotlight');
            card.addEventListener('pointermove', event => {
                const rect = card.getBoundingClientRect();
                card.style.setProperty('--mouse-x', `${event.clientX - rect.left}px`);
                card.style.setProperty('--mouse-y', `${event.clientY - rect.top}px`);
            }, { passive: true });
        });
    }

    document.addEventListener('click', event => {
        const seat = event.target.closest('.seat-cell.available');
        if (!seat) return;
        seat.classList.remove('seat-pop');
        void seat.offsetWidth;
        seat.classList.add('seat-pop');
        window.setTimeout(() => seat.classList.remove('seat-pop'), 420);
    });

    function pulseElement(element) {
        if (!element) return;
        element.classList.remove('ui-value-updated');
        void element.offsetWidth;
        element.classList.add('ui-value-updated');
        window.setTimeout(() => element.classList.remove('ui-value-updated'), 520);
    }

    function watchMutations(selector) {
        document.querySelectorAll(selector).forEach(element => {
            const observer = new MutationObserver(() => pulseElement(element));
            observer.observe(element, { childList: true, characterData: true, subtree: true });
        });
    }

    watchMutations('#totalPrice, #selectedSeatsList, #attempt-made, #attempt-left, .total-value');

    const countdown = document.getElementById('countdown');
    if (countdown) {
        const timerHost = countdown.closest('.timer-box') || countdown;
        const syncCountdownState = () => {
            const parts = countdown.textContent.trim().split(':').map(Number);
            if (parts.length !== 2 || parts.some(Number.isNaN)) return;
            const remaining = (parts[0] * 60) + parts[1];
            timerHost.classList.toggle('is-warning', remaining <= 180 && remaining > 60);
            timerHost.classList.toggle('is-danger', remaining <= 60);
        };

        syncCountdownState();
        new MutationObserver(syncCountdownState)
            .observe(countdown, { childList: true, characterData: true, subtree: true });
    }

    const resultBanner = document.getElementById('resultBanner');
    if (resultBanner && 'animate' in resultBanner) {
        let lastClassName = resultBanner.className;
        new MutationObserver(() => {
            if (resultBanner.className === lastClassName) return;
            lastClassName = resultBanner.className;

            resultBanner.animate([
                { boxShadow: '0 0 0 0 rgba(37, 99, 235, 0)' },
                { boxShadow: '0 0 0 8px rgba(37, 99, 235, .12)' },
                { boxShadow: '0 0 0 0 rgba(37, 99, 235, 0)' }
            ], { duration: 620, easing: 'ease-out' });

            const icon = resultBanner.querySelector('.result-icon');
            if (icon) {
                icon.animate([
                    { transform: 'scale(.84)', opacity: .72 },
                    { transform: 'scale(1.08)', opacity: 1 },
                    { transform: 'scale(1)', opacity: 1 }
                ], { duration: 420, easing: 'cubic-bezier(.2, .8, .2, 1)' });
            }

            if (resultBanner.classList.contains('result-danger')) {
                resultBanner.animate([
                    { transform: 'translateX(0)' },
                    { transform: 'translateX(-5px)' },
                    { transform: 'translateX(5px)' },
                    { transform: 'translateX(0)' }
                ], { duration: 310, easing: 'ease-out' });
            }
        }).observe(resultBanner, { attributes: true, attributeFilter: ['class'] });
    }

    const numberTargets = document.querySelectorAll('.dashboard-page .value-large, .dashboard-page .sub-stat-val, .dashboard-page .progress-bar');
    const numberFormatter = new Intl.NumberFormat('vi-VN');

    function animateNumberText(element) {
        if (element.dataset.counted === 'true') return;
        const text = element.textContent.trim();
        const match = text.match(/[\d.,]+/);
        if (!match) return;

        const raw = match[0];
        const target = Number(raw.replace(/\./g, '').replace(/,/g, ''));
        if (!Number.isFinite(target) || target <= 0 || target > 999999999999) return;

        element.dataset.counted = 'true';
        const prefix = text.slice(0, match.index);
        const suffix = text.slice(match.index + raw.length);
        const startedAt = performance.now();
        const duration = 920;

        function frame(now) {
            const progress = Math.min((now - startedAt) / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            const current = Math.round(target * eased);
            element.textContent = `${prefix}${numberFormatter.format(current)}${suffix}`;
            if (progress < 1) requestAnimationFrame(frame);
        }

        requestAnimationFrame(frame);
    }

    if ('IntersectionObserver' in window && numberTargets.length > 0) {
        const countObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (!entry.isIntersecting) return;
                animateNumberText(entry.target);
                observer.unobserve(entry.target);
            });
        }, { threshold: 0.45 });

        numberTargets.forEach(item => countObserver.observe(item));
    }
})();
