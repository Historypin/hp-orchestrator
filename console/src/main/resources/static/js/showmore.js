jQuery.fn.dataTable.render.showmore = function (cutoff, escapeHtml) {
    return function (d, type, row) {
        // Order, search and type get the original data
        if (type !== 'display') {
            return d;
        }

        if (typeof d !== 'number' && typeof d !== 'string') {
            return d;
        }

        d = d.toString(); // cast numbers

        if (d.length <= cutoff) {
            return d;
        }

        // Protect against uncontrolled HTML input
        if (escapeHtml) {
            var div = document.createElement('div');
            var text = document.createTextNode(d);
            div.appendChild(text);
            d = div.innerHTML;
        }

        return '<div class="text-container">' +
            '<div class="content hideContent"><pre>' + d + '</pre></div>' +
            '<div class="show-more"><a href="#">Show more</a></div>' +
            '</div>';
    };
};
