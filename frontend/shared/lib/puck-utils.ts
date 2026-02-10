import type { Data } from '@measured/puck';

/**
 * Converts legacy layout format { sections, order } to Puck { content, root, zones } format.
 * Returns null if the data is neither legacy nor valid Puck format.
 */
export function convertLegacyToPuck(data: Record<string, unknown>): Data | null {
  // Already Puck format
  if (Array.isArray(data.content)) {
    return {
      content: data.content,
      root: (data.root as Data['root']) || { props: { title: '' } },
      zones: (data.zones as Data['zones']) || {},
    };
  }

  // Legacy format: { sections: { key: { type, settings } }, order: [...] }
  if (data.sections && data.order && Array.isArray(data.order)) {
    const sections = data.sections as Record<
      string,
      { type: string; settings?: Record<string, unknown> }
    >;
    const order = data.order as string[];

    const content = order
      .filter((key) => sections[key])
      .map((key) => {
        const section = sections[key];
        const { type, settings = {} } = section;
        return {
          type,
          props: {
            id: `${type}-${key}`,
            ...settings,
          },
        };
      });

    return {
      content,
      root: { props: { title: '' } },
      zones: {},
    } as Data;
  }

  return null;
}
