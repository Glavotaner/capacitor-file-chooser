import { registerPlugin } from '@capacitor/core';

import type { FileChooserPlugin } from './definitions';

const FileChooser = registerPlugin<FileChooserPlugin>('FileChooser', {
  web: () => import('./web').then(m => new m.FileChooserWeb()),
});

export * from './definitions';
export { FileChooser };
