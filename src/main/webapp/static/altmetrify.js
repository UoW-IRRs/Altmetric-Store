if(window.attachEvent) {
    window.attachEvent('onload', altmetrify);
} else {
    if(window.onload) {
        var curronload = window.onload;

        window.onload = function() {
            curronload();
            altmetrify();
        };
    } else {
        window.onload = altmetrify;
    }
}

function altmetrify() {
    if (window.jQuery) {
        jQuery.each($(".altmetrics-citecounts"), function() {
            var parent = $(this);
            var handle = $(this).attr("data-handle");
            var provider = $(this).attr("data-provider");

            $.getJSON("/altmetrics/from-store.json?handle=" + handle + "&provider=" + provider,
                function(json) {
                    if (!json || !('metrics') in json) {
                        return true;
                    }
                    var numCitedBy = json['metrics']['citedby-count'];

                    if (parseInt(numCitedBy) < 1) {
                        return true;
                    }

                    var citeHtml = '<span class="bold">Scopus&#169; <span class="help" title="Scopus&#169; citation counts are updated weekly. The actual number of citing articles may be higher.">times cited</span>:</span> ';

                    var citationsLink = json['metrics']['scopus-citedby-link'];
                    if (citationsLink != null) {
                        citeHtml += '<a href="';
                        citeHtml += citationsLink;
                        citeHtml += '" class="citing-link" target="_new" title="View citing articles in Scopus&#169;" >';
                        citeHtml += numCitedBy;
                        citeHtml += '</a>';
                    } else {
                        citeHtml += numCitedBy;
                    }

                    var articleLink = json['metrics']['scopus-link'];
                    if (articleLink != null) {
                        citeHtml += '&nbsp;&nbsp;<small>(<a href="';
                        citeHtml += articleLink;
                        citeHtml += '" class="article-link" target="_new" title="View record in Scopus&#169;" >';
                        citeHtml += 'View record in Scopus&#169;';
                        citeHtml += '</a>)</small>';
                    }

                    parent.empty();
                    parent.append(citeHtml);
                    parent.removeClass('hidden');
                    parent.show('blind');
                    if (parent.children("a.citing-link")) {
                        parent.children("a.citing-link").click(function () {
                            if (_gaq) {
                                _gaq.push(['_trackEvent', 'Citecount links', 'Scopus citing items', handle]);
                            }
                        });
                    }
                    if (parent.children("a.article-link")) {
                        parent.children("a.article-link").click(function () {
                            if (_gaq) {
                                _gaq.push(['_trackEvent', 'Citecount links', 'Scopus article', handle]);
                            }
                        });
                    }
                });
        });
    }
    return true;
}