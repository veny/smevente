function replaceDiacritics(s, mode) {
    var $acc, $str, o, r, i

    // if character case for output string is not set set it to -1 (lower-case)
    if (typeof(mode)=='undefined') {
      mode = -1;
    }

    // conversion table. It can be copied from PHP source.
    $acc =	'É	Ê	Ë	š	Ì	Í	ƒ	œ	µ	Î	Ï	ž	Ð	Ÿ	Ñ	Ò	Ó	Ô	Š	£	Õ	Ö	Œ	¥	Ø	Ž	§	À	Ù	Á	Ú	Â	Û	Ã	Ü	Ä	Ý	';
    $str =	'E	E	E	s	I	I	f	o	m	I	I	z	D	Y	N	O	O	O	S	L	O	O	O	Y	O	Z	S	A	U	A	U	A	U	A	U	A	Y	';
    $acc+=	'Å	Æ	ß	Ç	à	È	á	â	û	Ĕ	ĭ	ņ	ş	Ÿ	ã	ü	ĕ	Į	Ň	Š	Ź	ä	ý	Ė	į	ň	š	ź	å	þ	ė	İ	ŉ	Ţ	Ż	æ	ÿ	';
    $str+=	'A	A	S	C	a	E	a	a	u	E	i	n	s	Y	a	u	e	I	N	S	Z	a	y	E	i	n	s	z	a	p	e	I	n	T	Z	a	y	';
    $acc+=	'Ę	ı	Ŋ	ţ	ż	ç	Ā	ę	Ĳ	ŋ	Ť	Ž	è	ā	Ě	ĳ	Ō	ť	ž	é	Ă	ě	Ĵ	ō	Ŧ	ſ	ê	ă	Ĝ	ĵ	Ŏ	ŧ	ë	Ą	ĝ	Ķ	ŏ	';
    $str+=	'E	l	n	t	z	c	A	e	I	n	T	Z	e	a	E	i	O	t	z	e	A	e	J	o	T	i	e	a	G	j	O	t	e	A	g	K	o	';
    $acc+=	'Ũ	ì	ą	Ğ	ķ	Ő	ũ	í	Ć	ğ	ĸ	ő	Ū	î	ć	Ġ	Ĺ	Œ	ū	ï	Ĉ	ġ	ĺ	œ	Ŭ	ð	ĉ	Ģ	Ļ	Ŕ	ŭ	ñ	Ċ	ģ	ļ	ŕ	Ů	';
    $str+=	'U	i	a	G	k	O	u	i	C	g	k	o	U	i	c	G	L	O	u	i	C	g	l	o	U	o	c	G	L	R	u	n	C	g	l	r	U	';
    $acc+=	'ò	ċ	Ĥ	Ľ	Ŗ	ů	ó	Č	ĥ	ľ	ŗ	Ű	ô	č	Ħ	Ŀ	Ř	ű	õ	Ď	ħ	ŀ	ř	Ų	ö	ď	Ĩ	Ł	Ś	ų	Đ	ĩ	ł	ś	Ŵ	ø	đ	';
    $str+=	'o	c	H	L	R	u	o	C	h	l	r	U	o	c	H	L	R	u	o	D	h	l	r	U	o	d	I	L	S	c	D	i	l	s	W	o	d	';
    $acc+=	'Ī	Ń	Ŝ	ŵ	ù	Ē	ī	ń	ŝ	Ŷ	Ə	ú	ē	Ĭ	Ņ	Ş	ŷ';
    $str+=	'I	N	S	w	u	E	i	n	s	Y	e	u	e	I	N	S	y';

    // If characters wasn't in conversion table and it is not a [a-zA-z0-9_-] convert it to this char
    o = '';

    // convert conversion tables into arrays
    var ta = $acc.split("\t");
    var ts = $str.split("\t");

    // iterate over conversion tables and replace every char in string
    for (i = 0; i < ta.length; i++) {
      r = new RegExp('[' + ta[i] + ']', 'g');
      s = s.replace(r, ts[i]);
    }

    // remove every character not found in conversion table
//    s = s.replace(/[^a-zA-Z0-9_-]/gi, o);

    // some optimization - you can change it if you change conversion tables
    // here I replace multiple underscores into one underscore and strip
    // underscores at beginning and end of string
//    s = s.replace(/[_]+/, '_');
//    s = s.replace(/^_*(.*?)_*$/gi, '$1')

    // return converted string
    if (mode == -1) {
        return s.toLowerCase();
    } else if (mode == 1) {
        return s.toUpperCase();
    } else {
        return s;
    }
}
