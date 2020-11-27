#!/bin/sh

/opt/pandoc /storage/"$1"/md.md -f markdown -t latex --pdf-engine=xelatex \
  -o /storage/"$1"/pdf.pdf \
  --resource-path /storage/"$1"/img 2>/storage/"$1"/log
