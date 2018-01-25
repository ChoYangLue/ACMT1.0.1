package com.tsuchiya.ken.acmt101;

import java.io.File;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.*;

/**
 * �t�@�C�����X�g�_�C�A���O�N���X
 * @author Iori
 *
 */
public class FileListDialog extends Activity 
	implements View.OnClickListener
			, DialogInterface.OnClickListener
	{

	private Context _parent = null;							//�e
	private File[] _dialog_file_list;						//���A�\�����Ă���t�@�C���̃��X�g
	private int _select_count = -1;							//�I�������C���f�b�N�X
	private onFileListDialogListener _listener = null;		//���X�i�[
	private boolean _is_directory_select = false;			//�f�B���N�g���I�������邩�H
	
	/**
	 * �f�B���N�g���I�������邩�H
	 * @param is
	 */
	public void setDirectorySelect(boolean is){
		_is_directory_select = is;
	}
	public boolean isDirectorySelect(){
		return _is_directory_select;
	}
	
	/**
	 * �I�����ꂽ�t�@�C�����擾
	 * @return
	 */
	public String getSelectedFileName(){
		String ret = "";
		if(_select_count < 0){
		
		}else{
			ret = _dialog_file_list[_select_count].getName();
		}
		return ret;
	}
	
	/**
	 * �t�@�C���I���_�C�A���O
	 * @param context �e
	 */
	public FileListDialog(Context context){
		_parent = context;
	}
	
	@Override
	public void onClick(View v) {
		// �����Ȃ�		
	}

	/**
	 * �_�C�A���O�̑I���C�x���g
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		//�I�����ꂽ�̂ňʒu��ۑ�
		_select_count = which;
		if((_dialog_file_list == null) || (_listener == null)){
		}else{
			File file = _dialog_file_list[which];
			
//			Util.outputDebugLog("getAbsolutePath : " + file.getAbsolutePath());
//			Util.outputDebugLog("getPath : " + file.getPath());
//			Util.outputDebugLog("getName : " + file.getName());
//			Util.outputDebugLog("getParent : " + file.getParent());
			
			if(file.isDirectory() && !isDirectorySelect()){
				//�I���������ڂ��f�B���N�g���ŁA�f�B���N�g���I�����Ȃ��ꍇ�͂�����x���X�g�\��
				show(file.getAbsolutePath(), file.getPath());
			}else{
				//����ȊO�͏I���Ȃ̂Őe�̃n���h���Ăяo��
				_listener.onClickFileList(file);
			}
		}
	}

	public void show(String path, String title){
		
		try{
			_dialog_file_list = new File(path).listFiles();
			if(_dialog_file_list == null){
				//NG
				if(_listener != null){
					//���X�i�[���o�^����Ă����ŌĂяo��
					_listener.onClickFileList(null);
				}
			}else{
				String[] list = new String[_dialog_file_list.length];
				int count = 0;
				String name = "";

				//�t�@�C�����̃��X�g�����
				for (File file : _dialog_file_list) {
					if(file.isDirectory()){
						//�f�B���N�g���̏ꍇ
						name = file.getName() + "/";
					}else{
						//�ʏ�̃t�@�C��
						name = file.getName();
					}
					list[count] = name;
					count++;
				}

				//�_�C�A���O�\��
				new AlertDialog.Builder(_parent).setTitle(title).setItems(list, this).show();
			}
		}catch(SecurityException se){
			System.out.println(se.getMessage());
		}catch(Exception e){
            System.out.println(e.getMessage());
		}
		
	}
	
	/**
	 * ���X�i�[�̃Z�b�g
	 * @param listener
	 */
	public void setOnFileListDialogListener(onFileListDialogListener listener){
		_listener = listener;
	}
	
	/**
	 * �N���b�N�C�x���g�̃C���^�[�t�F�[�X�N���X
	 * @author Iori
	 *
	 */
	public interface onFileListDialogListener{
		public void onClickFileList(File file);
	}

}

// http://relog.xii.jp/mt5r/2010/07/android.html