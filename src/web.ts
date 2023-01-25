import { WebPlugin } from '@capacitor/core';

import type { FileChooserPlugin } from './definitions';

export class FileChooserWeb extends WebPlugin implements FileChooserPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
