'use client';

import React from 'react';
import { Render, type Data } from '@measured/puck';
import { puckConfig } from '@shared/lib/puck-config';
import type { Product, Collection } from '@shared/lib/api';

interface PuckRendererProps {
  data: Data;
  storeSlug: string;
  product?: Product;
  products?: Product[];
  collection?: Collection;
  collections?: Collection[];
}

export default function PuckRenderer({
  data,
  storeSlug,
  product,
  products,
  collection,
  collections,
}: PuckRendererProps) {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  return (
    <Render
      config={puckConfig}
      data={data}
      metadata={{
        storeSlug,
        apiUrl,
        product,
        products,
        collection,
        collections,
      }}
    />
  );
}
