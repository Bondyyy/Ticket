(function () {
    const landing = document.querySelector('.landing-page');
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

    if (!landing || reduceMotion.matches) return;

    landing.classList.add('landing-motion-ready');

    const staggerGroups = [
        '.trust-grid .trust-card',
        '.feature-grid .feature-card',
        '.audience-section .audience-card',
        '.timeline-grid .step-card',
        '.event-grid .event-card'
    ];

    staggerGroups.forEach(selector => {
        document.querySelectorAll(selector).forEach((item, index) => {
            item.classList.add('reveal-on-scroll');
            item.style.setProperty('--reveal-delay', `${Math.min(index * 80, 480)}ms`);
        });
    });

    const workflow = document.getElementById('how-it-works');
    if (workflow) {
        workflow.classList.add('workflow-stack-enabled');
        workflow.querySelectorAll('.step-card').forEach((card, index) => {
            card.style.setProperty('--stack-index', index);
        });
    }

    const heroVisual = landing.querySelector('.hero-visual');
    const hero = landing.querySelector('.landing-hero');
    let ticking = false;

    function updateParallax() {
        ticking = false;
        if (!hero) return;

        const rect = hero.getBoundingClientRect();
        const viewport = window.innerHeight || 1;
        const progress = Math.max(0, Math.min(1, (viewport - rect.top) / (viewport + rect.height)));
        const bgY = `${Math.round(progress * 34)}px`;
        const visualY = `${Math.round(progress * -22)}px`;

        landing.style.setProperty('--hero-bg-y', bgY);
        if (heroVisual) {
            landing.style.setProperty('--hero-visual-y', visualY);
        }
    }

    function requestParallax() {
        if (ticking) return;
        ticking = true;
        requestAnimationFrame(updateParallax);
    }

    updateParallax();
    window.addEventListener('scroll', requestParallax, { passive: true });
    window.addEventListener('resize', requestParallax);

    const mediaItems = document.querySelectorAll('.event-media img, .dashboard-preview, .ticket-preview, .seat-preview');
    if ('IntersectionObserver' in window) {
        const mediaObserver = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                entry.target.classList.toggle('is-scroll-focused', entry.isIntersecting);
            });
        }, { threshold: 0.34 });

        mediaItems.forEach(item => mediaObserver.observe(item));
    }
})();
