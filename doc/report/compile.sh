rm *.bbl *.blg *.log *.aux
latex report.tex
bibtex report.aux
latex report.tex
latex report.tex
rm *.bbl *.blg *.log *.aux
dvips report.dvi
ps2pdf report.ps
rm *.dvi *.ps
mv report.pdf ../
xdg-open ../report.pdf
clear
