#!/bin/bash
set -e

# Create 512x512 Master Logo PNG using ImageMagick primitives
convert -size 512x512 xc:"#942911" \
  -fill none -stroke "#D97706" -strokewidth 8 -draw "roundrectangle 16,16 496,496 100,100" \
  -fill none -stroke "#FCD34D" -strokewidth 2 -draw "roundrectangle 28,28 484,484 88,88" \
  -fill none -stroke "#F59E0B" -strokewidth 4 -draw "circle 256,256 256,56" \
  -fill none -stroke "#D97706" -strokewidth 1.5 -draw "circle 256,256 256,70" \
  -fill "#FFFFFF" -stroke "#D97706" -strokewidth 4 -draw "path 'M 180,310 L 215,310 C 225,328 287,328 297,310 L 332,310 C 344,310 354,318 360,328 L 388,368 C 394,376 391,388 382,394 L 358,408 C 350,412 342,410 336,402 L 322,382 L 322,436 C 322,446 314,454 304,454 L 208,454 C 198,454 190,446 190,436 L 190,382 L 176,402 C 170,410 162,412 154,408 L 130,394 C 121,388 118,376 124,368 L 152,328 C 158,318 168,310 180,310 Z'" \
  -fill "#F59E0B" -stroke none -draw "path 'M 215,310 C 225,338 287,338 297,310 C 282,352 230,352 215,310 Z'" \
  -fill none -stroke "#94A3B8" -strokewidth 3 -draw "line 256,338 256,400" \
  -fill "#059669" -stroke "#FFFFFF" -strokewidth 3 -draw "circle 256,420 256,402" \
  -fill none -stroke "#FFFFFF" -strokewidth 3.5 -draw "path 'M 248,420 L 254,426 L 265,414'" \
  -fill "#FCD34D" -stroke "#78350F" -strokewidth 2.5 -draw "path 'M 256,70 C 252,90 244,125 244,160 C 244,185 252,205 256,220 C 260,205 268,185 268,160 C 268,125 260,90 256,70 Z'" \
  -fill "#F59E0B" -stroke "#78350F" -strokewidth 2.5 -draw "path 'M 244,175 C 225,170 185,150 178,105 C 174,80 186,85 190,95 C 198,120 220,150 242,158 Z'" \
  -fill "#F59E0B" -stroke "#78350F" -strokewidth 2.5 -draw "path 'M 268,175 C 287,170 327,150 334,105 C 338,80 326,85 322,95 C 314,120 292,150 270,158 Z'" \
  -fill "#FEF08A" -stroke "#78350F" -strokewidth 2 -draw "roundrectangle 238,216 274,228 4,4" \
  -fill "#F59E0B" -stroke "#78350F" -strokewidth 1.5 -draw "roundrectangle 252,228 260,290 3,3" \
  -fill "#FFFFFF" -stroke "#F59E0B" -strokewidth 3 -draw "path 'M 256,240 C 246,252 246,268 256,280 C 266,268 266,252 256,240 Z'" \
  -fill "#942911" -stroke none -draw "circle 256,260 256,254" \
  -fill "#FCD34D" -stroke none -draw "circle 256,260 256,257.5" \
  -fill "#FFFFFF" -stroke "#F59E0B" -strokewidth 2.5 -draw "path 'M 198,266 C 213,252 233,254 240,266 C 233,278 213,276 198,266 Z'" \
  -fill "#1E293B" -stroke none -draw "circle 220,266 220,261" \
  -fill "#FFFFFF" -stroke none -draw "circle 221.5,264.5 221.5,263" \
  -fill none -stroke "#F59E0B" -strokewidth 3.5 -draw "path 'M 196,255 C 212,243 232,247 240,257'" \
  -fill "#FFFFFF" -stroke "#F59E0B" -strokewidth 2.5 -draw "path 'M 314,266 C 299,252 279,254 272,266 C 279,278 299,276 314,266 Z'" \
  -fill "#1E293B" -stroke none -draw "circle 292,266 292,261" \
  -fill "#FFFFFF" -stroke none -draw "circle 290.5,264.5 290.5,263" \
  -fill none -stroke "#F59E0B" -strokewidth 3.5 -draw "path 'M 316,255 C 300,243 280,247 272,257'" \
  -fill "#DC2626" -stroke none -draw "circle 256,233 256,229" \
  -fill "#FB7185" -stroke "#F59E0B" -strokewidth 2 -draw "circle 125,270 125,252" \
  -fill "#FB7185" -stroke "#F59E0B" -strokewidth 2 -draw "circle 387,270 387,252" \
  -fill "#FCD34D" -stroke none -draw "circle 125,270 125,263" \
  -fill "#FCD34D" -stroke none -draw "circle 387,270 387,263" \
  "PNG32:app/src/main/res/drawable/logo.png"

echo "Generated app/src/main/res/drawable/logo.png"

# Now generate mipmap icons for all screen densities!
BASE="app/src/main/res"
SRC="$BASE/drawable/logo.png"

for spec in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  dir="$BASE/mipmap-${spec%:*}"
  size="${spec#*:}"
  
  # Clean up old webp files if any
  rm -f "$dir/ic_launcher.webp" "$dir/ic_launcher_round.webp"
  
  # Square launcher icon
  convert "$SRC" -resize "${size}x${size}!" "PNG32:$dir/ic_launcher.png"
  
  # Round launcher icon with circular alpha mask
  radius=$((size / 2))
  convert "$SRC" -resize "${size}x${size}!" \
    \( -size "${size}x${size}" xc:none -fill white \
       -draw "circle $radius,$radius $radius,0" \) \
    -alpha set -compose DstIn -composite "PNG32:$dir/ic_launcher_round.png"
done

echo "Generated all mipmap launcher icons successfully!"
