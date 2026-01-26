def key($lon;$lat;$S):
    ((($lon * $S) | floor) | tostring) + "," + ((($lat * $S) | floor) | tostring);

  # 量化尺度：1e-6
  ($S := 1000000) |

  # Point坐标集合（用 object 当 set）
  ($ptset :=
    reduce ($g[0].features[]
      | select(.geometry.type=="Point")
      | .geometry.coordinates as $c
      | key($c[0]; $c[1]; $S)
    ) as $k ({}; .[$k]=true)
  ) |

  # LineString 所有坐标点匹配统计
  ($all :=
    reduce ($g[0].features[]
      | select(.geometry.type=="LineString")
      | .geometry.coordinates[]
      | key(.[0]; .[1]; $S)
    ) as $k ({total:0, hit:0};
      .total += 1
      | if ($ptset[$k] // false) then .hit += 1 else . end
    )
  ) |

  # LineString 中间点匹配统计（排除首尾）
  ($mid :=
    reduce ($g[0].features[]
      | select(.geometry.type=="LineString")
      | .geometry.coordinates as $cs
      | if ($cs|length) > 2 then $cs[1:-1][] else empty end
      | key(.[0]; .[1]; $S)
    ) as $k ({total:0, hit:0};
      .total += 1
      | if ($ptset[$k] // false) then .hit += 1 else . end
    )
  ) |

  {
    scale: $S,
    points: ($g[0].features[] | select(.geometry.type=="Point") | 1) | add,
    linestrings: ($g[0].features[] | select(.geometry.type=="LineString") | 1) | add,
    all_coords: $all,
    all_match_rate: (if $all.total==0 then 0 else ($all.hit / $all.total) end),
    mid_coords: $mid,
    mid_match_rate: (if $mid.total==0 then 0 else ($mid.hit / $mid.total) end)
  }
