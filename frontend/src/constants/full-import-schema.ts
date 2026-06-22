// JSON Schema for the full topic import file
export const fullImportSchema = {
  $schema: 'https://json-schema.org/draft/2020-12/schema',
  $id: 'https://thi.de/mynd/schemas/full-import.schema.json',
  title: 'FullImportDto',
  description:
    'Schema for a full topic import file consumed by de.thi.mynd.topic.dto.importer.FullImportDto',
  type: 'object',
  additionalProperties: false,
  required: ['topics', 'associations'],
  properties: {
    topics: {
      type: 'array',
      items: { $ref: '#/$defs/ImportTopicDto' },
    },
    associations: {
      type: 'object',
      description:
        'Map keyed by a topic identifier (must match a topics[].identifier). Each value is a list of 1-3 other topic identifiers that this topic is associated with.',
      propertyNames: {
        type: 'string',
        minLength: 1,
      },
      additionalProperties: {
        type: 'array',
        minItems: 1,
        maxItems: 3,
        items: {
          type: 'string',
          minLength: 1,
        },
      },
    },
  },
  $defs: {
    ImportTopicDto: {
      type: 'object',
      additionalProperties: false,
      required: ['identifier', 'title', 'teaser'],
      properties: {
        identifier: {
          type: 'string',
          minLength: 1,
        },
        title: {
          type: 'string',
          minLength: 1,
        },
        teaser: {
          type: 'string',
          minLength: 1,
        },
        categories: {
          type: 'array',
          minItems: 1,
          maxItems: 2,
          items: {
            type: 'string',
          },
        },
        duration: {
          type: 'integer',
        },
        contentElements: {
          type: 'array',
          minItems: 1,
          maxItems: 12,
          items: { $ref: '#/$defs/ContentElement' },
        },
        indexCards: {
          type: 'array',
          maxItems: 20,
          items: { $ref: '#/$defs/ImportIndexCardDto' },
        },
      },
    },
    ImportIndexCardDto: {
      type: 'object',
      additionalProperties: false,
      required: ['question', 'answer'],
      properties: {
        question: {
          type: 'string',
          minLength: 1,
        },
        answer: {
          type: 'string',
          minLength: 1,
        },
      },
    },
    ContentElement: {
      type: 'object',
      description:
        "Discriminated union over 'type'. Allowed values: RTF, SPOTIFY_LINK, URI, YOUTUBE_LINK.",
      required: ['type'],
      oneOf: [
        { $ref: '#/$defs/RtfElement' },
        { $ref: '#/$defs/SpotifyLinkElement' },
        { $ref: '#/$defs/UriElement' },
        { $ref: '#/$defs/YouTubeLinkElement' },
      ],
    },
    RtfElement: {
      type: 'object',
      additionalProperties: false,
      required: ['type', 'title', 'rtfText'],
      properties: {
        type: { const: 'RTF' },
        title: {
          type: 'string',
          minLength: 1,
        },
        icon: {
          type: 'string',
          maxLength: 64,
        },
        rank: {
          type: 'integer',
        },
        rtfText: {
          type: 'string',
          minLength: 1,
        },
      },
    },
    SpotifyLinkElement: {
      type: 'object',
      additionalProperties: false,
      required: ['type', 'title', 'uri'],
      properties: {
        type: { const: 'SPOTIFY_LINK' },
        title: {
          type: 'string',
          minLength: 1,
        },
        icon: {
          type: 'string',
          maxLength: 64,
        },
        rank: {
          type: 'integer',
        },
        uri: {
          type: 'string',
          minLength: 1,
        },
      },
    },
    UriElement: {
      type: 'object',
      description:
        'INFERRED SHAPE - UriElement.java was not provided. Assumed to mirror SpotifyLinkElement (title + uri). Verify against the actual entity before relying on this.',
      additionalProperties: false,
      required: ['type', 'title', 'uri'],
      properties: {
        type: { const: 'URI' },
        title: {
          type: 'string',
          minLength: 1,
        },
        icon: {
          type: 'string',
          maxLength: 64,
        },
        rank: {
          type: 'integer',
        },
        uri: {
          type: 'string',
          minLength: 1,
        },
      },
    },
    YouTubeLinkElement: {
      type: 'object',
      description:
        'INFERRED SHAPE - YouTubeLinkElement.java was not provided. Assumed to mirror SpotifyLinkElement (title + uri). Verify against the actual entity before relying on this.',
      additionalProperties: false,
      required: ['type', 'title', 'uri'],
      properties: {
        type: { const: 'YOUTUBE_LINK' },
        title: {
          type: 'string',
          minLength: 1,
        },
        icon: {
          type: 'string',
          maxLength: 64,
        },
        rank: {
          type: 'integer',
        },
        uri: {
          type: 'string',
          minLength: 1,
        },
      },
    },
  },
};

export const fullImportSchemaText = JSON.stringify(fullImportSchema, null, 2);

export const buildFullImportSchemaText = (allowedCategories: string[]): string => {
  const schema = JSON.parse(fullImportSchemaText) as typeof fullImportSchema;
  const topicProps = schema.$defs.ImportTopicDto.properties as {
    categories: { items: unknown };
  };
  topicProps.categories.items =
    allowedCategories.length > 0 ? { type: 'string', enum: allowedCategories } : { type: 'string' };
  return JSON.stringify(schema, null, 2);
};
